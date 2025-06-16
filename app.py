import streamlit as st
import os
import json
import uuid
import random
import string
import hashlib
import platform

SECRETS_FILE = "secrets.txt"
GROUP_DIR = "groups"

# --- Setup ---
if not os.path.exists(SECRETS_FILE):
    with open(SECRETS_FILE, 'w') as f:
        f.write("users={}\ngroups={}\n")

if not os.path.exists(GROUP_DIR):
    os.makedirs(GROUP_DIR)


# --- Helper functions ---
def get_device_id():
    return str(uuid.getnode())


def hash_password(password):
    return hashlib.sha256(password.encode()).hexdigest()


def load_secrets():
    with open(SECRETS_FILE, "r") as f:
        lines = f.readlines()
        users = eval(lines[0].split("=", 1)[1].strip())
        groups = eval(lines[1].split("=", 1)[1].strip())
    return users, groups


def save_secrets(users, groups):
    with open(SECRETS_FILE, "w") as f:
        f.write(f"users={users}\ngroups={groups}\n")


def generate_group_code():
    return ''.join(random.choices(string.ascii_uppercase + string.digits, k=5))


def load_group_data(group_code):
    path = os.path.join(GROUP_DIR, f"{group_code}.json")
    if os.path.exists(path):
        with open(path, "r") as f:
            return json.load(f)
    return {"creator": "", "members": [], "expenses": []}


def save_group_data(group_code, data):
    path = os.path.join(GROUP_DIR, f"{group_code}.json")
    with open(path, "w") as f:
        json.dump(data, f, indent=2)


# --- Auth ---
device_id = get_device_id()
st.title("💸 Let’s Go Dutch")

st.sidebar.header("🔐 Login or Register")
password = st.sidebar.text_input("Enter Password", type="password")
action = st.sidebar.selectbox("Action", ["Login", "Register"])

users, groups = load_secrets()
auth_id = f"{device_id}-{hash_password(password)}"

if action == "Register":
    if auth_id in users:
        st.sidebar.warning("Already registered.")
    else:
        users[auth_id] = {"device_id": device_id}
        save_secrets(users, groups)
        st.sidebar.success("Registration successful!")

if action == "Login":
    if auth_id not in users:
        st.sidebar.error("Invalid credentials.")
        st.stop()
    else:
        st.sidebar.success("Logged in!")
        current_user = auth_id

        # Group Section
        st.header("👥 Group Management")
        group_action = st.radio("Choose action", ["Create Group", "Join Group", "Manage Group"])

        if group_action == "Create Group":
            group_code = generate_group_code()
            groups[group_code] = {"creator": current_user}
            save_secrets(users, groups)
            group_data = {
                "creator": current_user,
                "members": [current_user],
                "expenses": []
            }
            save_group_data(group_code, group_data)
            st.success(f"Group created! Your code is: `{group_code}`")

        elif group_action == "Join Group":
            join_code = st.text_input("Enter Group Code")
            if st.button("Join"):
                if join_code in groups:
                    group_data = load_group_data(join_code)
                    if current_user not in group_data["members"]:
                        group_data["members"].append(current_user)
                        save_group_data(join_code, group_data)
                        st.success("Joined group successfully!")
                    else:
                        st.info("Already in group.")
                else:
                    st.error("Group not found.")

        elif group_action == "Manage Group":
            st.subheader("Your Groups")
            user_groups = [code for code, g in groups.items() if current_user in load_group_data(code)["members"]]

            selected_group = st.selectbox("Select Group", user_groups)
            if selected_group:
                group_data = load_group_data(selected_group)
                members = group_data["members"]

                st.write(f"👥 Members: {len(members)}")
                st.write([m[-8:] for m in members]) # Short IDs

                if st.button("Delete Group") and current_user == group_data["creator"]:
                    os.remove(os.path.join(GROUP_DIR, f"{selected_group}.json"))
                    del groups[selected_group]
                    save_secrets(users, groups)
                    st.success("Group deleted.")
                    st.stop()

                st.subheader("➕ Add Expense")
                amount = st.number_input("Amount", min_value=0.0)
                description = st.text_input("Description")
                split_with = st.multiselect("Split With", [m for m in members if m != current_user])

                if st.button("Submit Expense"):
                    if not split_with or amount <= 0:
                        st.warning("Please enter valid amount and members.")
                    else:
                        per_person = round(amount / (len(split_with) + 1), 2)
                        expense = {
                            "by": current_user,
                            "amount": amount,
                            "description": description,
                            "split": [current_user] + split_with,
                            "per_person": per_person
                        }
                        group_data["expenses"].append(expense)
                        save_group_data(selected_group, group_data)
                        st.success("Expense added.")

                st.subheader("💰 Shared Expense Summary")
                balances = {uid: 0.0 for uid in members}
                for ex in group_data["expenses"]:
                    for uid in ex["split"]:
                        balances[uid] -= ex["per_person"]
                    balances[ex["by"]] += ex["amount"]

                st.write("### Balances (positive = owed to, negative = owes)")
                for uid, bal in balances.items():
                    name = "You" if uid == current_user else uid[-8:]
                    st.write(f"{name}: {'₹' + str(round(bal, 2))}")
