const listEl = document.getElementById("itemList");
const inputEl = document.getElementById("itemInput");
const errorEl = document.getElementById("error");

function showError(msg) {
    errorEl.textContent = msg;
    errorEl.classList.remove("hidden");
}

function clearError() {
    errorEl.classList.add("hidden");
}

async function loadItems() {
    clearError();
    listEl.innerHTML = "";

    try {
        const res = await fetch("/items");

        if (!res.ok) {
            throw new Error("Failed to load items");
        }

        const items = await res.json();

        items.forEach(item => {
            const li = document.createElement("li");
            li.textContent = item.name;

            const del = document.createElement("span");
            del.textContent = "✕";
            del.onclick = (e) => {
                e.stopPropagation();
                deleteItem(item.id);
            };

            li.appendChild(del);
            listEl.appendChild(li);
        });

    } catch (err) {
        showError(err.message);
    }
}

async function addItem() {
    const value = inputEl.value.trim();
    if (!value) return;

    clearError();

    try {
        const res = await fetch("/items", {
            method: "POST",
            body: value
        });

        if (res.status === 429) {
            throw new Error("Rate limit exceeded");
        }

        if (!res.ok) {
            throw new Error("Failed to add item");
        }

        inputEl.value = "";
        loadItems();

    } catch (err) {
        showError(err.message);
    }
}

async function deleteItem(id) {
    clearError();

    try {
        const res = await fetch(`/items/${id}`, {
            method: "DELETE"
        });

        if (!res.ok) {
            throw new Error("Failed to delete item");
        }

        loadItems();

    } catch (err) {
        showError(err.message);
    }
}

loadItems();
