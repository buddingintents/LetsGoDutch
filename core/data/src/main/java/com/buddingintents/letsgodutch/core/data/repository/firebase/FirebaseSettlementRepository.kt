package com.buddingintents.letsgodutch.core.data.repository.firebase

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.buddingintents.letsgodutch.core.data.repository.SettlementRepository
import com.buddingintents.letsgodutch.core.model.Balance
import com.buddingintents.letsgodutch.core.model.Expense
import com.buddingintents.letsgodutch.core.model.Group
import com.buddingintents.letsgodutch.core.model.Member
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.tasks.await

class FirebaseSettlementRepository(
    private val database: FirebaseDatabase,
    private val appContext: Context,
) : SettlementRepository {

    private val root = database.reference

    override suspend fun generateSettlementPdf(groupId: String, actorUserId: String): Result<String> {
        return runCatching {
            val group = ensureGroupAndOwner(groupId = groupId, actorUserId = actorUserId)
            val members = root.child("groupMembers").child(groupId).get().await().children
                .mapNotNull { it.toMemberOrNull() }
                .sortedBy { it.joinedAtEpochMs }
            val expenses = root.child("expenses").child(groupId).get().await().children
                .mapNotNull { it.toExpenseOrNull() }
                .sortedBy { it.createdAtEpochMs }
            val balances = root.child("balances").child(groupId).get().await().children
                .mapNotNull { it.toBalanceOrNull() }
                .sortedByDescending { it.netPaise }

            val involvedUserIds = mutableSetOf<String>()
            involvedUserIds.add(group.ownerUserId)
            members.forEach { involvedUserIds.add(it.userId) }
            expenses.forEach { expense ->
                involvedUserIds.add(expense.paidByUserId)
                involvedUserIds.add(expense.createdByUserId)
                involvedUserIds.addAll(expense.participantUserIds)
            }
            balances.forEach { involvedUserIds.add(it.userId) }

            val memberNameById = resolveDisplayNames(members = members, userIds = involvedUserIds)
            val file = generatePdfFile(
                group = group,
                members = members,
                expenses = expenses,
                balances = balances,
                memberNameById = memberNameById,
            )
            file.absolutePath
        }
    }

    override suspend fun dispatchSettlementPdfToMembers(
        groupId: String,
        actorUserId: String,
        pdfPath: String,
    ): Result<Unit> {
        return runCatching {
            val group = ensureGroupAndOwner(groupId = groupId, actorUserId = actorUserId)

            val file = File(pdfPath)
            check(file.exists() && file.isFile) { "Settlement PDF file not found." }

            val members = root.child("groupMembers").child(groupId).get().await().children
                .mapNotNull { it.toMemberOrNull() }
                .filter { it.active }
            check(members.isNotEmpty()) { "No active members to dispatch settlement PDF." }

            val now = System.currentTimeMillis()
            val settlementId = "stl_$now"

            val updates = mutableMapOf<String, Any?>(
                "settlementDispatch/$groupId/dispatchMode" to "LOCAL_OWNER_DEVICE",
                "settlementDispatch/$groupId/lastSettlementId" to settlementId,
                "settlementDispatch/$groupId/dispatchedAtEpochMs" to now,
                "settlementDispatch/$groupId/dispatchedByUserId" to actorUserId,
                "settlementDispatch/$groupId/memberCount" to members.size,
                "settlementDispatch/$groupId/members" to members.map { it.userId },
                "settlementDispatch/$groupId/history/$settlementId" to mapOf(
                    "groupId" to groupId,
                    "settlementId" to settlementId,
                    "dispatchMode" to "LOCAL_OWNER_DEVICE",
                    "dispatchedAtEpochMs" to now,
                    "dispatchedByUserId" to actorUserId,
                ),
            )

            members.forEach { member ->
                if (isManualMemberUserId(member.userId)) return@forEach
                val notificationId = root.child("notifications").child(member.userId).push().key
                    ?: "n_${now}_${member.userId}"
                updates["notifications/${member.userId}/$notificationId"] = mapOf(
                    "type" to "SETTLEMENT_PDF",
                    "groupId" to groupId,
                    "settlementId" to settlementId,
                    "title" to "Settlement report ready",
                    "body" to "Settlement for ${group.name} is complete. Owner can share the PDF.",
                    "dispatchMode" to "LOCAL_OWNER_DEVICE",
                    "delivered" to true,
                    "deliveredAtEpochMs" to now,
                    "createdAtEpochMs" to now,
                    "byUserId" to actorUserId,
                    "read" to false,
                )
            }
            root.updateChildren(updates).await()
        }
    }

    override suspend fun markGroupSettled(groupId: String, actorUserId: String): Result<Unit> {
        return runCatching {
            ensureGroupAndOwner(groupId = groupId, actorUserId = actorUserId)

            val hasExpenses = root.child("expenses").child(groupId).get().await().hasChildren()
            check(hasExpenses) { "No expenses to settle." }

            val dispatchSnapshot = root.child("settlementDispatch").child(groupId).get().await()
            val settlementId = dispatchSnapshot.child("lastSettlementId").getValue(String::class.java).orEmpty()
            check(settlementId.isNotBlank()) {
                "Settlement blocked: PDF is not dispatched to members."
            }

            val updates = mapOf<String, Any?>(
                "expenses/$groupId" to null,
                "balances/$groupId" to null,
                "settlementDispatch/$groupId/settledAtEpochMs" to System.currentTimeMillis(),
                "settlementDispatch/$groupId/settledByUserId" to actorUserId,
            )
            root.updateChildren(updates).await()
        }
    }

    private suspend fun ensureGroupAndOwner(groupId: String, actorUserId: String): Group {
        val group = root.child("groups").child(groupId).get().await().toGroupOrNull()
            ?: error("Group not found.")
        val actorRole = root.child("groupMembers")
            .child(groupId)
            .child(actorUserId)
            .child("role")
            .get()
            .await()
            .getValue(String::class.java)
            .orEmpty()
        check(actorRole == "OWNER" || group.ownerUserId == actorUserId) { "Only an owner can settle the group." }
        return group
    }

    private suspend fun resolveDisplayNames(
        members: List<Member>,
        userIds: Set<String>,
    ): Map<String, String> {
        val memberById = members.associateBy { it.userId }
        val profileNameById = mutableMapOf<String, String>()

        userIds.filter { it.isNotBlank() }.forEach { userId ->
            val profileSnapshot = runCatching {
                root.child("users").child(userId).child("profile").get().await()
            }.getOrNull() ?: return@forEach
            val profileName = profileSnapshot.childString("displayName").trim()
            if (profileName.isNotBlank()) {
                profileNameById[userId] = profileName
            }
        }

        return userIds.filter { it.isNotBlank() }.associateWith { userId ->
            val member = memberById[userId]
            resolveBestDisplayName(
                userId = userId,
                profileName = profileNameById[userId].orEmpty(),
                memberName = member?.displayName.orEmpty(),
                memberEmail = member?.email.orEmpty(),
            )
        }
    }

    private fun resolveBestDisplayName(
        userId: String,
        profileName: String,
        memberName: String,
        memberEmail: String,
    ): String {
        val emailAlias = memberEmail.substringBefore("@")
        val candidates = listOf(profileName, memberName, emailAlias)
        for (candidate in candidates) {
            val normalized = candidate.trim()
            if (normalized.isBlank()) continue
            if (normalized.equals(userId, ignoreCase = true)) continue
            if (normalized.equals("member", ignoreCase = true)) continue
            return normalized
        }
        return "Member"
    }

    private fun generatePdfFile(
        group: Group,
        members: List<Member>,
        expenses: List<Expense>,
        balances: List<Balance>,
        memberNameById: Map<String, String>,
    ): File {
        val outputDir = File(appContext.filesDir, "settlements").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val safeGroupName = group.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outputFile = File(outputDir, "settlement_${safeGroupName}_$timestamp.pdf")

        val document = PdfDocument()
        // Fresh Mint palette: charcoal surfaces with mint highlights.
        val pageBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.parseColor("#243046")
        }
        val headerFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#121C28")
        }
        val headerStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#3DD68C")
        }
        val sectionFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#1C2333")
        }
        val sectionStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#2DB878")
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3DD68C")
            textSize = 42f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EAF4F1")
            textSize = 28f
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EAF4F1")
            textSize = 20f
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DDF7EA")
            textSize = 16f
        }
        val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3DD68C")
            textSize = 24f
            isFakeBoldText = true
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#4A5969")
            strokeWidth = 2f
        }
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#0D7A6E")
        }
        val tableOddRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#1A2533")
        }
        val tableEvenRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#243046")
        }
        val barTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#314054")
        }
        val positiveBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#3DD68C")
        }
        val negativeBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#FF7B7B")
        }

        val generatedAt = nowDisplay()
        val groupName = group.name.ifBlank { "Unnamed Group" }
        val ownerName = readFriendlyName(group.ownerUserId, memberNameById)
        val contentBottomLimit = PAGE_HEIGHT - PAGE_MARGIN - 38f
        val suggestedTransactions = buildSuggestedTransactions(balances)

        var pageNumber = 1
        var page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create(),
        )
        var canvas = page.canvas
        var y = drawHeader(
            canvas = canvas,
            groupName = groupName,
            ownerName = ownerName,
            pageNumber = pageNumber,
            titlePaint = titlePaint,
            subtitlePaint = subtitlePaint,
            smallPaint = smallPaint,
            pageBorderPaint = pageBorderPaint,
            headerFillPaint = headerFillPaint,
            headerStrokePaint = headerStrokePaint,
        )

        fun startNewPage() {
            document.finishPage(page)
            pageNumber += 1
            page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create(),
            )
            canvas = page.canvas
            y = drawHeader(
                canvas = canvas,
                groupName = groupName,
                ownerName = ownerName,
                pageNumber = pageNumber,
                titlePaint = titlePaint,
                subtitlePaint = subtitlePaint,
                smallPaint = smallPaint,
                pageBorderPaint = pageBorderPaint,
                headerFillPaint = headerFillPaint,
                headerStrokePaint = headerStrokePaint,
            )
        }

        fun ensureSpace(requiredHeight: Float) {
            if (y + requiredHeight <= contentBottomLimit) return
            startNewPage()
        }

        fun rowsThatFitForCurrentPage(yStart: Float): Int {
            val availableForRows = contentBottomLimit - (yStart + LEDGER_SECTION_HEADER_BLOCK_HEIGHT)
            return (availableForRows / LEDGER_ROW_HEIGHT).toInt().coerceAtLeast(1)
        }

        val totalAmount = expenses.sumOf { it.amountPaise }
        val activeMembersCount = members.count { it.active }

        ensureSpace(248f)
        y = drawSnapshotSection(
            canvas = canvas,
            yStart = y,
            totalAmount = totalAmount,
            totalTransactions = expenses.size,
            activeMembers = activeMembersCount,
            bodyPaint = bodyPaint,
            sectionTitlePaint = sectionTitlePaint,
            smallPaint = smallPaint,
            linePaint = linePaint,
            sectionFillPaint = sectionFillPaint,
            sectionStrokePaint = sectionStrokePaint,
        )

        ensureSpace(432f)
        y = drawPaidSharePieSection(
            canvas = canvas,
            yStart = y,
            expenses = expenses,
            memberNameById = memberNameById,
            sectionTitlePaint = sectionTitlePaint,
            bodyPaint = bodyPaint,
            smallPaint = smallPaint,
            sectionFillPaint = sectionFillPaint,
            sectionStrokePaint = sectionStrokePaint,
        )

        val balanceRows = balances.take(8).size.coerceAtLeast(1)
        val balanceSectionHeight = 120f + (balanceRows * 44f)
        ensureSpace(balanceSectionHeight + SECTION_GAP)
        y = drawBalanceBars(
            canvas = canvas,
            yStart = y,
            balances = balances,
            memberNameById = memberNameById,
            sectionTitlePaint = sectionTitlePaint,
            bodyPaint = bodyPaint,
            linePaint = linePaint,
            sectionFillPaint = sectionFillPaint,
            sectionStrokePaint = sectionStrokePaint,
            barTrackPaint = barTrackPaint,
            positiveBarPaint = positiveBarPaint,
            negativeBarPaint = negativeBarPaint,
        )

        val suggestedRows = suggestedTransactions.take(10).size.coerceAtLeast(1)
        val suggestedSectionHeight = 120f + (suggestedRows * 44f)
        ensureSpace(suggestedSectionHeight + SECTION_GAP)
        y = drawSuggestedTransactionsSection(
            canvas = canvas,
            yStart = y,
            suggestions = suggestedTransactions,
            memberNameById = memberNameById,
            sectionTitlePaint = sectionTitlePaint,
            bodyPaint = bodyPaint,
            linePaint = linePaint,
            sectionFillPaint = sectionFillPaint,
            sectionStrokePaint = sectionStrokePaint,
        )

        ensureSpace(LEDGER_SECTION_HEADER_BLOCK_HEIGHT + LEDGER_ROW_HEIGHT)
        if (expenses.isEmpty()) {
            val sectionHeight = LEDGER_SECTION_HEADER_BLOCK_HEIGHT +
                LEDGER_ROW_HEIGHT +
                LEDGER_SECTION_BOTTOM_PADDING
            y = drawLedgerSectionHeader(
                canvas = canvas,
                yStart = y,
                title = "Expense Ledger",
                sectionHeight = sectionHeight,
                sectionTitlePaint = sectionTitlePaint,
                smallPaint = smallPaint,
                linePaint = linePaint,
                sectionFillPaint = sectionFillPaint,
                sectionStrokePaint = sectionStrokePaint,
                tableHeaderPaint = tableHeaderPaint,
            )
            val tableLeft = PAGE_MARGIN + CARD_INSET + 8f
            val tableRight = PAGE_WIDTH - PAGE_MARGIN - CARD_INSET - 8f
            canvas.drawRect(
                tableLeft,
                y,
                tableRight,
                y + LEDGER_ROW_HEIGHT,
                tableOddRowPaint,
            )
            canvas.drawRect(
                tableLeft,
                y,
                tableRight,
                y + LEDGER_ROW_HEIGHT,
                linePaint,
            )
            canvas.drawText("No expenses available.", tableLeft + 14f, y + 29f, bodyPaint)
            y += LEDGER_ROW_HEIGHT
        } else {
            var expenseIndex = 0
            while (expenseIndex < expenses.size) {
                val rowsThisPage = minOf(rowsThatFitForCurrentPage(y), expenses.size - expenseIndex)
                val sectionHeight = LEDGER_SECTION_HEADER_BLOCK_HEIGHT +
                    (rowsThisPage * LEDGER_ROW_HEIGHT) +
                    LEDGER_SECTION_BOTTOM_PADDING
                y = drawLedgerSectionHeader(
                    canvas = canvas,
                    yStart = y,
                    title = if (expenseIndex == 0) "Expense Ledger" else "Expense Ledger (cont.)",
                    sectionHeight = sectionHeight,
                    sectionTitlePaint = sectionTitlePaint,
                    smallPaint = smallPaint,
                    linePaint = linePaint,
                    sectionFillPaint = sectionFillPaint,
                    sectionStrokePaint = sectionStrokePaint,
                    tableHeaderPaint = tableHeaderPaint,
                )
                repeat(rowsThisPage) {
                    val expense = expenses[expenseIndex]
                    y = drawLedgerRow(
                        canvas = canvas,
                        rowTop = y,
                        expense = expense,
                        rowIndex = expenseIndex,
                        memberNameById = memberNameById,
                        bodyPaint = bodyPaint,
                        smallPaint = smallPaint,
                        linePaint = linePaint,
                        evenRowPaint = tableEvenRowPaint,
                        oddRowPaint = tableOddRowPaint,
                    )
                    expenseIndex += 1
                }
                if (expenseIndex < expenses.size) {
                    startNewPage()
                }
            }
        }

        ensureSpace(74f)
        val footerLeft = PAGE_MARGIN + CARD_INSET + 8f
        val footerRight = PAGE_WIDTH - PAGE_MARGIN - CARD_INSET - 8f
        canvas.drawLine(footerLeft, y, footerRight, y, linePaint)
        y += 30f
        canvas.drawText(
            "Generated by Let's Go Dutch on $generatedAt",
            footerLeft,
            y,
            smallPaint,
        )

        document.finishPage(page)
        FileOutputStream(outputFile).use { output ->
            document.writeTo(output)
        }
        document.close()
        return outputFile
    }

    private fun drawHeader(
        canvas: Canvas,
        groupName: String,
        ownerName: String,
        pageNumber: Int,
        titlePaint: Paint,
        subtitlePaint: Paint,
        smallPaint: Paint,
        pageBorderPaint: Paint,
        headerFillPaint: Paint,
        headerStrokePaint: Paint,
    ): Float {
        val pageRect = RectF(
            PAGE_MARGIN,
            PAGE_MARGIN,
            PAGE_WIDTH - PAGE_MARGIN,
            PAGE_HEIGHT - PAGE_MARGIN,
        )
        canvas.drawRect(pageRect, pageBorderPaint)

        val headerRect = RectF(
            pageRect.left + CARD_INSET,
            pageRect.top + CARD_INSET,
            pageRect.right - CARD_INSET,
            pageRect.top + HEADER_HEIGHT,
        )
        canvas.drawRoundRect(headerRect, CARD_CORNER, CARD_CORNER, headerFillPaint)
        canvas.drawRoundRect(headerRect, CARD_CORNER, CARD_CORNER, headerStrokePaint)

        canvas.drawText("Let's Go Dutch", headerRect.left + 22f, headerRect.top + 58f, titlePaint)
        canvas.drawText(groupName.take(42), headerRect.left + 22f, headerRect.top + 98f, subtitlePaint)
        canvas.drawText("Owner: ${ownerName.take(30)}", headerRect.left + 22f, headerRect.top + 132f, smallPaint)
        drawRightAlignedText(
            canvas = canvas,
            text = "Page $pageNumber",
            rightX = headerRect.right - 20f,
            y = headerRect.top + 34f,
            paint = smallPaint,
        )
        return headerRect.bottom + SECTION_GAP
    }

    private fun drawSnapshotSection(
        canvas: Canvas,
        yStart: Float,
        totalAmount: Long,
        totalTransactions: Int,
        activeMembers: Int,
        bodyPaint: Paint,
        sectionTitlePaint: Paint,
        smallPaint: Paint,
        linePaint: Paint,
        sectionFillPaint: Paint,
        sectionStrokePaint: Paint,
    ): Float {
        val card = drawSectionCard(
            canvas = canvas,
            yStart = yStart,
            title = "Settlement Snapshot",
            height = 226f,
            sectionTitlePaint = sectionTitlePaint,
            sectionFillPaint = sectionFillPaint,
            sectionStrokePaint = sectionStrokePaint,
        )
        var rowY = card.top + 86f
        val leftX = card.left + 22f
        val rightX = card.right - 22f
        val rows = listOf(
            "Total Expenses" to totalAmount.toInrDisplay(),
            "Total Transactions" to totalTransactions.toString(),
            "Active Members" to activeMembers.toString(),
        )
        rows.forEachIndexed { index, row ->
            canvas.drawText(row.first, leftX, rowY, bodyPaint)
            drawRightAlignedText(canvas, row.second, rightX, rowY, bodyPaint)
            if (index < rows.lastIndex) {
                canvas.drawLine(leftX, rowY + 12f, rightX, rowY + 12f, linePaint)
            }
            rowY += 44f
        }
        canvas.drawText("Currency: INR", leftX, card.bottom - 22f, smallPaint)
        return card.bottom + SECTION_GAP
    }

    private fun drawPaidSharePieSection(
        canvas: Canvas,
        yStart: Float,
        expenses: List<Expense>,
        memberNameById: Map<String, String>,
        sectionTitlePaint: Paint,
        bodyPaint: Paint,
        smallPaint: Paint,
        sectionFillPaint: Paint,
        sectionStrokePaint: Paint,
    ): Float {
        val card = drawSectionCard(
            canvas = canvas,
            yStart = yStart,
            title = "Who Paid (Pie)",
            height = 418f,
            sectionTitlePaint = sectionTitlePaint,
            sectionFillPaint = sectionFillPaint,
            sectionStrokePaint = sectionStrokePaint,
        )
        val contentTop = card.top + 70f

        val paidTotals = expenses.groupBy { it.paidByUserId }.mapValues { (_, list) -> list.sumOf { it.amountPaise } }
        if (paidTotals.isEmpty()) {
            canvas.drawText("No expenses available for pie chart.", card.left + 22f, contentTop + 28f, bodyPaint)
            return card.bottom + SECTION_GAP
        }

        val totalPaid = paidTotals.values.sum().coerceAtLeast(1L)
        val centerX = card.left + 200f
        val centerY = card.top + 220f
        val radius = 124f
        val oval = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        val colors = listOf(
            Color.parseColor("#3DD68C"),
            Color.parseColor("#0D7A6E"),
            Color.parseColor("#2DB878"),
            Color.parseColor("#6B7A8D"),
            Color.parseColor("#5BE0A4"),
            Color.parseColor("#58C7B4"),
            Color.parseColor("#A5EED0"),
        )

        val sortedPaidTotals = paidTotals.entries.sortedByDescending { it.value }
        var startAngle = -90f
        sortedPaidTotals.forEachIndexed { index, entry ->
            val sweep = 360f * (entry.value.toFloat() / totalPaid.toFloat())
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = colors[index % colors.size]
            }
            canvas.drawArc(oval, startAngle, sweep, true, paint)
            startAngle += sweep
        }

        var legendY = contentTop + 8f
        sortedPaidTotals.take(6).forEachIndexed { index, entry ->
            val legendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors[index % colors.size] }
            val legendLeft = card.left + 430f
            canvas.drawRect(legendLeft, legendY - 14f, legendLeft + 22f, legendY + 8f, legendPaint)
            val pct = (entry.value * 100.0) / totalPaid.toDouble()
            val memberName = readFriendlyName(entry.key, memberNameById)
            val label = "${memberName.take(18)}  ${entry.value.toInrDisplay()}  ${"%.1f".format(Locale.US, pct)}%"
            canvas.drawText(label.take(50), legendLeft + 34f, legendY, smallPaint)
            legendY += 28f
        }

        if (sortedPaidTotals.size > 6) {
            val extra = sortedPaidTotals.size - 6
            canvas.drawText("+$extra more member(s)", card.left + 464f, legendY, smallPaint)
        }
        return card.bottom + SECTION_GAP
    }

    private fun drawBalanceBars(
        canvas: Canvas,
        yStart: Float,
        balances: List<Balance>,
        memberNameById: Map<String, String>,
        sectionTitlePaint: Paint,
        bodyPaint: Paint,
        linePaint: Paint,
        sectionFillPaint: Paint,
        sectionStrokePaint: Paint,
        barTrackPaint: Paint,
        positiveBarPaint: Paint,
        negativeBarPaint: Paint,
    ): Float {
        val topBalances = balances.take(8)
        val rowCount = topBalances.size.coerceAtLeast(1)
        val sectionHeight = 120f + (rowCount * 44f)
        val card = drawSectionCard(
            canvas = canvas,
            yStart = yStart,
            title = "Net Balances (Bar)",
            height = sectionHeight,
            sectionTitlePaint = sectionTitlePaint,
            sectionFillPaint = sectionFillPaint,
            sectionStrokePaint = sectionStrokePaint,
        )

        if (topBalances.isEmpty()) {
            canvas.drawText("No balances available.", card.left + 22f, card.top + 98f, bodyPaint)
            return card.bottom + SECTION_GAP
        }

        val maxAbs = topBalances.maxOfOrNull { abs(it.netPaise) }?.coerceAtLeast(1L) ?: 1L
        var y = card.top + 74f
        val barLeft = card.left + 270f
        val barRight = card.right - 176f
        val maxBarWidth = (barRight - barLeft).coerceAtLeast(1f)

        topBalances.forEach { balance ->
            val name = readFriendlyName(balance.userId, memberNameById).take(20)
            canvas.drawText(name, card.left + 22f, y + 18f, bodyPaint)

            canvas.drawRect(barLeft, y + 2f, barRight, y + 22f, barTrackPaint)
            val width = ((abs(balance.netPaise).toFloat() / maxAbs.toFloat()) * maxBarWidth).coerceAtLeast(6f)
            val activeBarPaint = if (balance.netPaise >= 0L) positiveBarPaint else negativeBarPaint
            canvas.drawRect(barLeft, y + 2f, barLeft + width, y + 22f, activeBarPaint)

            drawRightAlignedText(
                canvas = canvas,
                text = balance.netPaise.toInrDisplay(),
                rightX = card.right - 22f,
                y = y + 18f,
                paint = bodyPaint,
            )
            y += 44f
        }

        canvas.drawLine(card.left + 22f, y - 6f, card.right - 22f, y - 6f, linePaint)
        return card.bottom + SECTION_GAP
    }

    private fun drawSuggestedTransactionsSection(
        canvas: Canvas,
        yStart: Float,
        suggestions: List<SettlementSuggestion>,
        memberNameById: Map<String, String>,
        sectionTitlePaint: Paint,
        bodyPaint: Paint,
        linePaint: Paint,
        sectionFillPaint: Paint,
        sectionStrokePaint: Paint,
    ): Float {
        val topSuggestions = suggestions.take(10)
        val rowCount = topSuggestions.size.coerceAtLeast(1)
        val sectionHeight = 120f + (rowCount * 44f)
        val card = drawSectionCard(
            canvas = canvas,
            yStart = yStart,
            title = "Suggested Transactions",
            height = sectionHeight,
            sectionTitlePaint = sectionTitlePaint,
            sectionFillPaint = sectionFillPaint,
            sectionStrokePaint = sectionStrokePaint,
        )

        if (topSuggestions.isEmpty()) {
            canvas.drawText("No settlement transfers required.", card.left + 22f, card.top + 98f, bodyPaint)
            return card.bottom + SECTION_GAP
        }

        var y = card.top + 74f
        topSuggestions.forEach { suggestion ->
            val payer = readFriendlyName(suggestion.fromUserId, memberNameById).take(18)
            val receiver = readFriendlyName(suggestion.toUserId, memberNameById).take(18)
            canvas.drawText("$payer  ->  $receiver", card.left + 22f, y + 18f, bodyPaint)
            drawRightAlignedText(
                canvas = canvas,
                text = suggestion.amountPaise.toInrDisplay(),
                rightX = card.right - 22f,
                y = y + 18f,
                paint = bodyPaint,
            )
            canvas.drawLine(card.left + 22f, y + 26f, card.right - 22f, y + 26f, linePaint)
            y += 44f
        }
        return card.bottom + SECTION_GAP
    }

    private fun drawLedgerSectionHeader(
        canvas: Canvas,
        yStart: Float,
        title: String,
        sectionHeight: Float,
        sectionTitlePaint: Paint,
        smallPaint: Paint,
        linePaint: Paint,
        sectionFillPaint: Paint,
        sectionStrokePaint: Paint,
        tableHeaderPaint: Paint,
    ): Float {
        val card = drawSectionCard(
            canvas = canvas,
            yStart = yStart,
            title = title,
            height = sectionHeight,
            sectionTitlePaint = sectionTitlePaint,
            sectionFillPaint = sectionFillPaint,
            sectionStrokePaint = sectionStrokePaint,
        )

        val tableLeft = PAGE_MARGIN + CARD_INSET + 8f
        val tableRight = PAGE_WIDTH - PAGE_MARGIN - CARD_INSET - 8f
        val headerTop = card.top + 66f
        canvas.drawRect(tableLeft, headerTop, tableRight, headerTop + LEDGER_HEADER_HEIGHT, tableHeaderPaint)
        canvas.drawRect(tableLeft, headerTop, tableRight, headerTop + LEDGER_HEADER_HEIGHT, linePaint)

        canvas.drawText("Title", tableLeft + 14f, headerTop + 24f, smallPaint)
        canvas.drawText("Paid By", tableLeft + 336f, headerTop + 24f, smallPaint)
        canvas.drawText("Shared With", tableLeft + 514f, headerTop + 24f, smallPaint)
        drawRightAlignedText(
            canvas = canvas,
            text = "Amount",
            rightX = tableRight - 16f,
            y = headerTop + 24f,
            paint = smallPaint,
        )
        return headerTop + LEDGER_HEADER_HEIGHT
    }

    private fun drawLedgerRow(
        canvas: Canvas,
        rowTop: Float,
        expense: Expense,
        rowIndex: Int,
        memberNameById: Map<String, String>,
        bodyPaint: Paint,
        smallPaint: Paint,
        linePaint: Paint,
        evenRowPaint: Paint,
        oddRowPaint: Paint,
    ): Float {
        val tableLeft = PAGE_MARGIN + CARD_INSET + 8f
        val tableRight = PAGE_WIDTH - PAGE_MARGIN - CARD_INSET - 8f
        val rowBottom = rowTop + LEDGER_ROW_HEIGHT
        val fillPaint = if (rowIndex % 2 == 0) evenRowPaint else oddRowPaint

        canvas.drawRect(tableLeft, rowTop, tableRight, rowBottom, fillPaint)
        canvas.drawRect(tableLeft, rowTop, tableRight, rowBottom, linePaint)

        val title = expense.title.take(34)
        val paidBy = readFriendlyName(expense.paidByUserId, memberNameById).take(16)
        val sharedWith = formatParticipantNames(expense.participantUserIds, memberNameById)
        val amount = expense.amountPaise.toInrDisplay()

        canvas.drawText(title, tableLeft + 14f, rowTop + 29f, bodyPaint)
        canvas.drawText(paidBy, tableLeft + 336f, rowTop + 29f, bodyPaint)
        canvas.drawText(sharedWith, tableLeft + 514f, rowTop + 29f, smallPaint)
        drawRightAlignedText(canvas, amount, tableRight - 16f, rowTop + 29f, bodyPaint)
        return rowBottom
    }

    private fun drawSectionCard(
        canvas: Canvas,
        yStart: Float,
        title: String,
        height: Float,
        sectionTitlePaint: Paint,
        sectionFillPaint: Paint,
        sectionStrokePaint: Paint,
    ): RectF {
        val card = RectF(
            PAGE_MARGIN + CARD_INSET,
            yStart,
            PAGE_WIDTH - PAGE_MARGIN - CARD_INSET,
            yStart + height,
        )
        canvas.drawRoundRect(card, CARD_CORNER, CARD_CORNER, sectionFillPaint)
        canvas.drawRoundRect(card, CARD_CORNER, CARD_CORNER, sectionStrokePaint)
        canvas.drawText(title, card.left + 22f, card.top + 40f, sectionTitlePaint)
        return card
    }

    private fun drawRightAlignedText(
        canvas: Canvas,
        text: String,
        rightX: Float,
        y: Float,
        paint: Paint,
    ) {
        val startX = rightX - paint.measureText(text)
        canvas.drawText(text, startX, y, paint)
    }

    private fun readFriendlyName(userId: String, memberNameById: Map<String, String>): String {
        return memberNameById[userId]
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "Member"
    }

    private fun formatParticipantNames(
        participantUserIds: List<String>,
        memberNameById: Map<String, String>,
    ): String {
        val names = participantUserIds
            .map { readFriendlyName(it, memberNameById) }
            .distinct()
        if (names.isEmpty()) return "-"
        val joined = names.joinToString(", ")
        return if (joined.length <= 34) joined else "${joined.take(31)}..."
    }

    private fun buildSuggestedTransactions(balances: List<Balance>): List<SettlementSuggestion> {
        val creditors = balances
            .filter { it.netPaise > 0L }
            .map { MutableSettlementParty(userId = it.userId, amountPaise = it.netPaise) }
            .sortedByDescending { it.amountPaise }
            .toMutableList()
        val debtors = balances
            .filter { it.netPaise < 0L }
            .map { MutableSettlementParty(userId = it.userId, amountPaise = abs(it.netPaise)) }
            .sortedByDescending { it.amountPaise }
            .toMutableList()

        val suggestions = mutableListOf<SettlementSuggestion>()
        var creditorIndex = 0
        var debtorIndex = 0
        while (creditorIndex < creditors.size && debtorIndex < debtors.size) {
            val creditor = creditors[creditorIndex]
            val debtor = debtors[debtorIndex]
            val transferAmount = minOf(creditor.amountPaise, debtor.amountPaise)
            if (transferAmount > 0L) {
                suggestions += SettlementSuggestion(
                    fromUserId = debtor.userId,
                    toUserId = creditor.userId,
                    amountPaise = transferAmount,
                )
            }
            creditor.amountPaise -= transferAmount
            debtor.amountPaise -= transferAmount
            if (creditor.amountPaise <= 0L) creditorIndex += 1
            if (debtor.amountPaise <= 0L) debtorIndex += 1
        }
        return suggestions
    }

    private fun Long.toInrDisplay(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return formatter.format(this.toDouble() / 100.0)
    }

    private fun nowDisplay(): String {
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date())
    }
}

private data class MutableSettlementParty(
    val userId: String,
    var amountPaise: Long,
)

private data class SettlementSuggestion(
    val fromUserId: String,
    val toUserId: String,
    val amountPaise: Long,
)

private fun isManualMemberUserId(userId: String): Boolean {
    return userId.startsWith("guest_")
}

private const val PAGE_WIDTH = 1080
private const val PAGE_HEIGHT = 1920
private const val PAGE_MARGIN = 34f
private const val HEADER_HEIGHT = 228f
private const val SECTION_GAP = 22f
private const val CARD_INSET = 14f
private const val CARD_CORNER = 18f
private const val LEDGER_HEADER_HEIGHT = 36f
private const val LEDGER_ROW_HEIGHT = 44f
private const val LEDGER_SECTION_HEADER_BLOCK_HEIGHT = 102f
private const val LEDGER_SECTION_BOTTOM_PADDING = 18f
