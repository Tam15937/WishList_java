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
            currentUserId: null,
            lists: [],
            stompClient: null,
            connected: false,
            currentSubscription: null,
            wishlistItems: [],
            currentView: 'check', // check | create | edit
            editingList: null,
            togglingItems: {}
        };
    },
    computed: {
        selectedList() {
            return this.lists.find(l => l.id === this.selectedListId);
        },
        canEditDelete() {
            return this.selectedList &&
                   this.selectedList.user &&
                   this.selectedList.user.id === this.currentUserId;
        }
    },
    methods: {
        loadCurrentUser() {
            const cookie = document.cookie.split('; ').find(r => r.startsWith('user_id='));
            this.currentUserId = cookie ? parseInt(cookie.split('=')[1]) : 1;
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
        connectWebSocket() {
            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);
            this.stompClient.connect({}, frame => {
                console.log('WebSocket подключён:', frame);
                this.connected = true;
                this.stompClient.subscribe('/topic/global', message => {
                    const update = JSON.parse(message.body);
                    if (update.blockKey === 'lists-overview') this.loadLists();
                });
            }, err => {
                console.error('WebSocket ошибка:', err);
                this.connected = false;
                setTimeout(() => this.connectWebSocket(), 5000);
            });
        },
        sendGlobalListsUpdate() {
            if (!this.stompClient || !this.connected) return;
            this.stompClient.send(
                '/app/global.update', {}, JSON.stringify({
                    blockKey: 'lists-overview',
                    payload: JSON.stringify({ action: 'refresh_lists' })
                })
            );
        },
        sendCategoryUpdate(blockKey, categoryId = this.selectedListId) {
            if (!this.stompClient || !this.connected || !categoryId) return;
            this.stompClient.send(
                `/app/category.update.${categoryId}`, {},
                JSON.stringify({
                    categoryId,
                    blockKey,
                    payload: JSON.stringify({ action: 'refresh_items' })
                })
            );
        },
        async loadListItems(listId) {
            try {
                const res = await fetch(`/items/list/${listId}`);
                if (!res.ok) throw new Error('Ошибка загрузки элементов');
                this.wishlistItems = await res.json();
            } catch (e) {
                alert('Ошибка загрузки подарков.');
            }
        },
        async toggleItem(id) {
            this.togglingItems[id] = true;
            try {
                const res = await fetch(`/items/${id}/toggle?userId=${this.currentUserId}`, { method: 'POST' });
                if (!res.ok) throw new Error('Ошибка при изменении статуса');
                await this.loadListItems(this.selectedListId);
                this.sendCategoryUpdate('items-refresh');
            } catch (e) {
                alert(e.message);
            } finally {
                this.togglingItems[id] = false;
            }
        },
        selectList(list) {
            this.selectedListId = list.id;
            this.currentView = 'check';

            if (this.currentSubscription) this.currentSubscription.unsubscribe();
            this.currentSubscription = this.stompClient.subscribe(`/topic/category.${list.id}`, msg => {
                const update = JSON.parse(msg.body);
                if (update.blockKey === 'items-refresh') this.loadListItems(list.id);
            });
            this.loadListItems(list.id);
        },
        async createList(data) {
            try {
                const res = await fetch('/lists', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        name: data.name,
                        user: { id: this.currentUserId },
                        items: data.gifts
                    })
                });
                if (!res.ok) throw new Error('Ошибка создания списка');
                const newList = await res.json();
                alert('Список создан!');
                await this.loadLists();
                this.currentView = 'check';
                this.selectList(newList);
                this.sendGlobalListsUpdate();
            } catch (e) {
                alert('Ошибка: ' + e.message);
            }
        },
        async updateList(data) {
            try {
                const res = await fetch(`/lists/${data.listId}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        name: data.name,
                        user: { id: this.currentUserId },
                        items: data.gifts
                    })
                });
                if (!res.ok) throw new Error('Ошибка обновления');
                alert('Список обновлён!');
                await this.loadLists();
                this.currentView = 'check';
                this.selectedListId = data.listId;
                this.loadListItems(data.listId);
                this.sendGlobalListsUpdate();
                this.sendCategoryUpdate('items-refresh', data.listId);
            } catch (e) {
                alert(e.message);
            }
        },
        async deleteList(id) {
            if (!confirm('Удалить список?')) return;
            try {
                const res = await fetch(`/lists/${id}`, { method: 'DELETE' });
                if (!res.ok) throw new Error('Ошибка удаления');
                alert('Список удалён!');
                this.selectedListId = null;
                this.wishlistItems = [];
                await this.loadLists();
                this.sendGlobalListsUpdate();
            } catch (e) {
                alert(e.message);
            }
        },
        startCreateList() {
            this.currentView = 'create';
            this.selectedListId = null;
            this.wishlistItems = [];
        },
        cancelCreate() {
            this.currentView = 'check';
        },
        editList(list) {
            this.editingList = list;
            this.currentView = 'edit';
        },
        cancelEdit() {
            this.currentView = 'check';
            this.editingList = null;
        },
        logout() {
            this.clearAuthCookies();
            this.clearAppData();
            this.redirectToLogin();
        },
        clearAuthCookies() {
            const cookies = ['user_id', 'auth_token', 'refresh_token', 'JSESSIONID', 'XSRF-TOKEN'];
            cookies.forEach(c =>
                document.cookie = `${c}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`
            );
        },
        clearAppData() {
            this.lists = [];
            this.selectedListId = null;
            this.wishlistItems = [];
            this.currentView = 'check';
            this.editingList = null;
            if (localStorage) localStorage.clear();
            if (sessionStorage) sessionStorage.clear();
        },
        redirectToLogin() {
            fetch('/auth/logout', { method: 'POST', credentials: 'include' })
                .finally(() => {
                    window.location.href = '/';
                    setTimeout(() => window.location.reload(), 100);
                });
        }
    },
    mounted() {
        this.loadCurrentUser();
        this.loadLists();
        this.connectWebSocket();
    },
    beforeUnmount() {
        if (this.currentSubscription) this.currentSubscription.unsubscribe();
        if (this.stompClient) this.stompClient.disconnect();
    },
    template: `
        <div class="container">
            <aside class="sidebar left">
                <h2>Wish листы</h2>
                <ul class="user-items">
                    <li v-if="lists.length === 0"><em>Списков нет</em></li>
                    <li v-for="list in lists"
                        :key="list.id"
                        :class="{ selected: list.id === selectedListId }"
                        @click="selectList(list)">
                        {{ list.name }}
                    </li>
                </ul>
            </aside>

            <div class="right-column">
                <div class="right-buttons">
                    <button @click="logout" id="logoutBtn">Выйти</button>
                    <button @click="startCreateList" id="createListBtn">Создать свой лист</button>
                </div>

                <main class="main-content">
                    <CheckListForm
                        v-if="currentView === 'check'"
                        :selected-list="selectedList"
                        :wishlist-items="wishlistItems"
                        :current-user_id="currentUserId"
                        @toggle-item="toggleItem"
                    />

                    <CreateListForm
                        v-else-if="currentView === 'create'"
                        :current-user_id="currentUserId"
                        @create-list="createList"
                        @cancel-create="cancelCreate"
                    />

                    <EditListForm
                        v-else-if="currentView === 'edit'"
                        :editing-list="editingList"
                        :current-user_id="currentUserId"
                        @update-list="updateList"
                        @cancel-edit="cancelEdit"
                    />

                    <div id="deleteSection" v-if="canEditDelete && selectedList && currentView === 'check'">
                        <button @click="editList(selectedList)" class="btn-edit">Редактировать список</button>
                        <button @click="deleteList(selectedList.id)" class="btn-delete">Удалить список</button>
                    </div>
                </main>
            </div>
        </div>
    `
};

createApp(App).mount('#app');
