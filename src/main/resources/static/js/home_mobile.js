let lists = [];
let selectedListId = null;
let currentUser_id = null;

function loadCurrentUser() {
    const cookie = document.cookie.split('; ').find(row => row.startsWith('user_id='));
    if (cookie) currentUser_id = parseInt(cookie.split('=')[1]);
    else currentUser_id = 1;
}

async function loadLists() {
    try {
        const res = await fetch('/lists');
        if (!res.ok) throw new Error('Ошибка загрузки списков');
        lists = await res.json();
        renderUsers();
        renderWishlist();
        renderDeleteButton();
    } catch (e) {
        console.error('Ошибка загрузки списков:', e);
        alert('Не удалось загрузить списки.');
    }
}

function renderUsers() {
    const ul = document.getElementById('userItems');
    ul.innerHTML = '';

    if (lists.length === 0) {
        ul.innerHTML = '<li><em>Списков пока нет</em></li>';
        return;
    }

    lists.forEach(list => {
        const li = document.createElement('li');
        li.textContent = list.name || '(без названия)';
        li.className = (list.id === selectedListId) ? 'selected' : '';
        li.style.cursor = 'pointer';
        li.style.padding = '8px';
        li.style.borderBottom = '1px solid #eee';
        li.dataset.id = list.id;

        li.onclick = () => {
            selectedListId = list.id;
            renderUsers();
            loadListItems(list.id);
            renderDeleteButton();
        };

        ul.appendChild(li);
    });
}

let currentItems = [];

async function loadListItems(listId) {
    try {
        const res = await fetch(`/items/list/${listId}`);
        if (!res.ok) throw new Error('Не удалось загрузить элементы списка');
        currentItems = await res.json();
        renderWishlist();
    } catch (e) {
        console.error('Ошибка:', e);
        alert('Ошибка загрузки подарков.');
    }
}

function renderWishlist() {
    const block = document.getElementById('wishlistContent');
    const list = lists.find(l => l.id === selectedListId);

    if (!list) {
        block.innerHTML = '<p>Выберите список слева, чтобы увидеть его wishlist.</p>';
        return;
    }

    let html = `<h2>Wishlist: ${list.name}</h2><ul style="list-style:none; padding-left:0;">`;
    currentItems.forEach(item => {
        const checked = item.taken ? 'checked' : '';
        let linkHtml = '';
        if (
            item.link &&
            typeof item.link === 'string' &&
            item.link.trim() !== '' &&
            /^https?:\/\//i.test(item.link.trim())
        ) {
            const urlEscaped = item.link.trim().replace(/"/g, '&quot;');
            linkHtml = `<div style="margin-left: 28px; margin-top: 4px;">
                <a href="${urlEscaped}" target="_blank" rel="noopener noreferrer"
                    style="padding: 4px 8px; border: 1px solid #2196f3; border-radius: 4px; font-size: 0.9em; color: #2196f3; text-decoration: none; display: inline-block;">
                    ссылка
                </a>
            </div>`;
        }
        html += `<li style="margin-bottom: 12px;">
            <label style="display: flex; align-items: center;">
                <input type="checkbox" data-item-id="${item.id}" ${checked} />
                <span class="${item.taken ? 'taken' : ''}" style="margin-left: 8px;">${item.name}</span>
            </label>
            ${linkHtml}
        </li>`;
    });
    html += '</ul>';
    block.innerHTML = html;

    block.querySelectorAll('input[type=checkbox]').forEach(checkbox => {
        checkbox.addEventListener('change', async e => {
            const itemId = e.target.getAttribute('data-item-id');
            try {
                const res = await fetch(`/items/${itemId}/toggle?userId=${currentUser_id}`, { method: 'POST' });
                if (!res.ok) throw new Error('Ошибка при изменении статуса');

                if (selectedListId) {
                    loadListItems(selectedListId);
                }
            } catch (err) {
                alert(err.message);
                e.target.checked = !e.target.checked;
            }
        });
    });
}

function renderDeleteButton() {
    const deleteSection = document.getElementById('deleteSection');
    deleteSection.innerHTML = '';
    if (selectedListId === null) return;

    const list = lists.find(l => l.id === selectedListId);
    if (!list) return;

    const listOwnerId = list.user?.id || list.user_id;
    if (!listOwnerId || !currentUser_id) return;
    if (String(listOwnerId) !== String(currentUser_id)) return;

    const editBtn = document.createElement('button');
    editBtn.textContent = 'Редактировать список';
    editBtn.style.backgroundColor = '#4caf50';
    editBtn.style.color = 'white';
    editBtn.style.marginBottom = '10px';
    editBtn.style.padding = '8px 12px';
    editBtn.style.border = 'none';
    editBtn.style.borderRadius = '4px';
    editBtn.style.cursor = 'pointer';
    editBtn.onclick = () => {
        const listToEdit = lists.find(l => l.id === selectedListId);
        showEditListForm(listToEdit);
    };
    deleteSection.appendChild(editBtn);

    const delBtn = document.createElement('button');
    delBtn.textContent = 'Удалить список';
    delBtn.style.backgroundColor = '#e53935';
    delBtn.style.color = 'white';
    delBtn.style.padding = '8px 12px';
    delBtn.style.border = 'none';
    delBtn.style.borderRadius = '4px';
    delBtn.style.cursor = 'pointer';
    delBtn.onclick = () => {
        if (confirm('Вы уверены, что хотите удалить этот список?')) {
            deleteList(selectedListId);
        }
    };
    deleteSection.appendChild(delBtn);
}

async function deleteList(id) {
    try {
        const res = await fetch(`/lists/${id}`, { method: 'DELETE' });
        if (res.ok) {
            alert('Список удалён!');
            selectedListId = null;
            await loadLists();
            document.getElementById('wishlistContent').innerHTML = '<p>Выберите список слева.</p>';
            document.getElementById('deleteSection').innerHTML = '';
        } else {
            alert('Ошибка удаления.');
        }
    } catch (e) {
        alert('Ошибка сети при удалении');
        console.error(e);
    }
}

function showCreateListForm() {
    selectedListId = null;
    const block = document.getElementById('wishlistContent');
    block.innerHTML = `
        <form id="createListForm">
            <input type="text" id="listName" placeholder="Название списка" maxlength="30" required style="width: 100%; padding: 10px; margin-bottom: 15px; border: 1px solid #ccc; border-radius: 4px;" />
            <div id="giftsContainer"></div>
            <button type="button" class="addGiftBtn" style="margin-bottom: 10px; padding: 8px 12px; background: #2196f3; color: white; border: none; border-radius: 4px; cursor: pointer;">Добавить подарок</button>
            <button type="submit" class="saveListBtn" style="padding: 10px 15px; background: #4caf50; color: white; border: none; border-radius: 4px; cursor: pointer;">Создать список</button>
        </form>
    `;

    const giftsContainer = document.getElementById('giftsContainer');
    const addGiftBtn = block.querySelector('.addGiftBtn');
    const form = block.querySelector('#createListForm');

    addGiftInput();

    addGiftBtn.onclick = () => addGiftInput();

    form.onsubmit = async (e) => {
        e.preventDefault();
        const listName = document.getElementById('listName').value.trim();
        if (!listName) { alert("Введите название списка!"); return; }

        const giftBlocks = giftsContainer.querySelectorAll('.giftBlock');
        const gifts = [];
        for (const block of giftBlocks) {
            const nameInput = block.querySelector('.giftInputName');
            const linkInput = block.querySelector('.giftInputLink');
            const nameVal = nameInput.value.trim();
            let linkVal = linkInput.value.trim();
            if (nameVal) {
                if (linkVal && !/^https?:\/\//i.test(linkVal)) { alert("Ссылка должна начинаться с http:// или https://"); return; }
                gifts.push({ name: nameVal, taken: false, link: linkVal });
            }
        }
        if (gifts.length === 0) { alert("Добавьте хотя бы один подарок!"); return; }

        try {
            const response = await fetch('/lists', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: listName,
                    user: { id: currentUser_id },
                    items: gifts // отправляем подарки в массиве
                })
            });
            if (!response.ok) throw new Error('Ошибка при создании списка');

            const createdList = await response.json();
            alert('Список создан!');
            await loadLists();
            selectedListId = createdList.id;
            renderUsers();
            loadListItems(createdList.id);
            renderDeleteButton();
            block.innerHTML = '<p>Список создан.</p>';
        } catch (err) {
            alert('Ошибка: ' + err.message);
        }
    };
}

function showEditListForm(list) {
    selectedListId = list.id;
    const block = document.getElementById('wishlistContent');
    block.innerHTML = `
        <form id="editListForm">
            <input type="text" id="listName" placeholder="Название списка" maxlength="30" required
                style="width: 100%; padding: 10px; margin-bottom: 15px; border: 1px solid #ccc; border-radius: 4px;" />
            <div id="giftsContainer"></div>
            <button type="button" class="addGiftBtn"
                style="margin-bottom: 10px; padding: 8px 12px; background: #2196f3; color: white; border: none; border-radius: 4px; cursor: pointer;">
                Добавить подарок
            </button>
            <button type="submit" class="saveListBtn"
                style="padding: 10px 15px; background: #4caf50; color: white; border: none; border-radius: 4px; cursor: pointer;">
                Сохранить изменения
            </button>
        </form>
    `;

    const giftsContainer = document.getElementById('giftsContainer');
    const addGiftBtn = block.querySelector('.addGiftBtn');
    const form = block.querySelector('#editListForm');

    document.getElementById('listName').value = list.name || '';

    // Загружаем подарки текущего списка с сервера
    fetch(`/items/list/${list.id}`)
        .then(r => r.json())
        .then(items => items.forEach(item => addGiftInput(item.name, item.link)))
        .catch(() => alert('Ошибка загрузки подарков'));

    addGiftBtn.onclick = () => addGiftInput();

    form.onsubmit = async (e) => {
        e.preventDefault();
        const listName = document.getElementById('listName').value.trim();
        if (!listName) {
            alert("Введите название списка!");
            return;
        }
        const giftBlocks = giftsContainer.querySelectorAll('.giftBlock');
        const gifts = [];
        for (const block of giftBlocks) {
            const nameInput = block.querySelector('.giftInputName');
            const linkInput = block.querySelector('.giftInputLink');
            const nameVal = nameInput.value.trim();
            let linkVal = linkInput.value.trim();
            if (nameVal) {
                if (linkVal && !/^https?:\/\//i.test(linkVal)) {
                    alert("Ссылка должна начинаться с http:// или https://");
                    return;
                }
                gifts.push({ name: nameVal, link: linkVal });
            }
        }
        if (gifts.length === 0) {
            alert("Добавьте хотя бы один подарок!");
            return;
        }

        try {
            const response = await fetch(`/lists/${selectedListId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: listName,
                    user: { id: currentUser_id },
                    items: gifts,
                })
            });
            if (!response.ok) throw new Error('Ошибка при обновлении списка');

            alert('Список успешно обновлён!');
            selectedListId = null;
            await loadLists();
            document.getElementById('wishlistContent').innerHTML = '<p>Выберите список слева.</p>';
            document.getElementById('deleteSection').innerHTML = '';
        } catch (err) {
            alert('Ошибка: ' + err.message);
        }
    };
}

function addGiftInput(name = '', link = '') {
    const giftsContainer = document.getElementById('giftsContainer');
    const block = document.createElement('div');
    block.className = 'giftBlock';
    block.style.marginBottom = '12px';
    block.style.display = 'flex';
    block.style.flexDirection = 'column';

    const inputName = document.createElement('input');
    inputName.type = 'text';
    inputName.className = 'giftInputName';
    inputName.placeholder = `Подарок ${giftsContainer.children.length + 1}`;
    inputName.maxLength = 40;
    inputName.value = name;

    const inputLink = document.createElement('input');
    inputLink.type = 'text';
    inputLink.className = 'giftInputLink';
    inputLink.placeholder = 'Ссылка (опционально)';
    inputLink.style.marginTop = '4px';
    inputLink.value = link;

    block.appendChild(inputName);
    block.appendChild(inputLink);
    giftsContainer.appendChild(block);
}

document.addEventListener('DOMContentLoaded', () => {
    loadCurrentUser();

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.onclick = () => {
            document.cookie = 'username=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;';
            document.cookie = 'user_id=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;';
            window.location.href = '/login';
        };
    }

    const createListBtn = document.getElementById('createListBtn');
    if (createListBtn) {
        createListBtn.onclick = () => {
            selectedListId = null;
            renderUsers();
            showCreateListForm();
        };
    }

    loadLists();
});
