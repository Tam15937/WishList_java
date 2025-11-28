const { createApp } = Vue;

const App = {
    components: {
        CheckListForm,
        EditListForm,
        CreateListForm
    },
    data() {
        return {
            selectedListId: null,
            currentUser_id: null,
            lists: [],
            wishlistItems: [],
            currentView: 'check', // 'check', 'create', 'edit'
            editingList: null
        }
    },
    computed: {
        selectedList() {
            return this.lists.find(list => list.id === this.selectedListId);
        },
        canEditDelete() {
            if (!this.selectedList || !this.selectedList.user) return false;
            return this.selectedList.user.id === this.currentUser_id;
        }
    },
    methods: {
        loadCurrentUser() {
            const cookie = document.cookie.split('; ').find(row => row.startsWith('user_id='));
            if (cookie) this.currentUser_id = parseInt(cookie.split('=')[1]);
            else this.currentUser_id = 1;
        },
        async loadLists() {
            try {
                const res = await fetch('/lists');
                if (!res.ok) throw new Error('Ошибка загрузки списков');
                this.lists = await res.json();
            } catch (e) {
                console.error('Ошибка:', e);
                alert('Не удалось загрузить списки.');
            }
        },
        async loadListItems(listId) {
            try {
                const res = await fetch(`/items/list/${listId}`);
                if (!res.ok) throw new Error('Не удалось загрузить элементы списка');
                this.wishlistItems = await res.json();
            } catch (e) {
                console.error('Ошибка:', e);
                alert('Ошибка загрузки подарков.');
            }
        },
        async toggleItem(itemId) {
            try {
                const res = await fetch(`/items/${itemId}/toggle?userId=${this.currentUser_id}`, {
                    method: 'POST'
                });
                if (!res.ok) throw new Error('Ошибка при изменении статуса');
                await this.loadListItems(this.selectedListId);
            } catch (err) {
                alert(err.message);
            }
        },
        selectList(list) {
            this.selectedListId = list.id;
            this.currentView = 'check';
            this.loadListItems(list.id);
        },
        async createList(listData) {
            try {
                const response = await fetch('/lists', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        name: listData.name,
                        user: { id: this.currentUser_id },
                        items: listData.gifts
                    })
                });
                if (!response.ok) throw new Error('Ошибка при создании списка');

                const createdList = await response.json();
                alert('Список создан!');
                await this.loadLists();
                this.currentView = 'check';
                this.selectList(createdList);
            } catch (err) {
                alert('Ошибка: ' + err.message);
            }
        },
        async updateList(listData) {
            try {
                const response = await fetch(`/lists/${listData.listId}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        name: listData.name,
                        user: { id: this.currentUser_id },
                        items: listData.gifts
                    })
                });
                if (!response.ok) throw new Error('Ошибка при обновлении списка');

                alert('Список успешно обновлён!');
                await this.loadLists();
                this.currentView = 'check';
                this.selectedListId = listData.listId;
                this.loadListItems(listData.listId);
            } catch (err) {
                alert('Ошибка: ' + err.message);
            }
        },
        editList(list) {
            this.editingList = list;
            this.currentView = 'edit';
        },
        cancelEdit() {
            this.currentView = 'check';
            this.editingList = null;
        },
        startCreateList() {
            this.currentView = 'create';
            this.selectedListId = null;
            this.wishlistItems = [];
        },
        cancelCreate() {
            this.currentView = 'check';
        },
        async deleteList(listId) {
            if (!confirm('Вы уверены, что хотите удалить этот список?')) return;

            try {
                const res = await fetch(`/lists/${listId}`, { method: 'DELETE' });
                if (res.ok) {
                    alert('Список удалён!');
                    this.selectedListId = null;
                    this.wishlistItems = [];
                    await this.loadLists();
                } else {
                    alert('Ошибка удаления.');
                }
            } catch (e) {
                alert('Ошибка сети.');
            }
        },
        logout() {
            alert('Выход отключен.');
        }
    },
    mounted() {
        this.loadCurrentUser();
        this.loadLists();
    },
    template: `
        <div class="container">
            <aside class="sidebar left">
                <h2>Wish листы</h2>
                <ul class="user-items">
                    <li v-if="lists.length === 0"><em>Списков пока нет</em></li>
                    <li v-for="list in lists"
                        :key="list.id"
                        :class="{ selected: list.id === selectedListId }"
                        @click="selectList(list)">
                        {{ list.name }}
                    </li>
                </ul>
            </aside>

            <main class="main-content">
                <CheckListForm
                    v-if="currentView === 'check'"
                    :selected-list="selectedList"
                    :wishlist-items="wishlistItems"
                    :current-user_id="currentUser_id"
                    @toggle-item="toggleItem"
                />
                <CreateListForm
                    v-else-if="currentView === 'create'"
                    :current-user_id="currentUser_id"
                    @create-list="createList"
                    @cancel-create="cancelCreate"
                />
                <EditListForm
                    v-else-if="currentView === 'edit'"
                    :editing-list="editingList"
                    :current-user_id="currentUser_id"
                    @update-list="updateList"
                    @cancel-edit="cancelEdit"
                />
            </main>

            <aside class="sidebar right">
                <div style="display: flex; flex-direction: column; height: 100%;">
                    <!-- Верхняя часть - основные кнопки -->
                    <div style="flex: 1;">
                        <button @click="startCreateList">Создать свой лист</button>

                        <div v-if="canEditDelete && selectedList && currentView === 'check'">
                            <button @click="editList(selectedList)" class="btn-edit">Редактировать список</button>
                            <button @click="deleteList(selectedList.id)" class="btn-delete">Удалить список</button>
                        </div>
                    </div>

                    <!-- Нижняя часть - кнопка выхода -->
                    <div style="margin-top: auto;">
                        <form @submit.prevent="logout">
                            <button type="submit" style="background: #f44336;">Выйти</button>
                        </form>
                    </div>
                </div>
            </aside>
        </div>
    `
};

createApp(App).mount('#app');