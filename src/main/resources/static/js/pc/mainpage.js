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
            stompClient: null,  // WebSocket клиент
            connected: false,   // статус подключения
            currentSubscription: null,
            wishlistItems: [],
            currentView: 'check', // 'check', 'create', 'edit'
            editingList: null,
            togglingItems: {}
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
        // Подключение WebSocket
        connectWebSocket() {
          const socket = new SockJS('/ws');  // эндпоинт из WebSocketConfig
          this.stompClient = Stomp.over(socket);

          this.stompClient.connect({},
            (frame) => {
              console.log('WebSocket подключён:', frame);
              this.connected = true;

              // Подписка на ГЛОБАЛЬНЫЕ обновления списков (всех клиентов)
              this.stompClient.subscribe('/topic/global', (message) => {
                const update = JSON.parse(message.body);
                if (update.blockKey === 'lists-overview') {
                  console.log('Обновляем список списков');
                  this.loadLists();  // твоя функция перезагрузки
                }
              });
            },
            (error) => {
              console.error('WebSocket ошибка:', error);
              this.connected = false;
              // Переподключение через 5 сек
              setTimeout(() => this.connectWebSocket(), 5000);
            }
          );
        },

        // Отправить глобальное обновление списков (вызывать после create/delete)
        sendGlobalListsUpdate() {
          if (!this.stompClient || !this.connected) return;

          const message = {
            blockKey: 'lists-overview',
            payload: JSON.stringify({ action: 'refresh_lists' })
          };

          this.stompClient.send('/app/global.update', {}, JSON.stringify(message));
          console.log('Отправлено глобальное обновление списков');
        },

        sendCategoryUpdate(blockKey, categoryId = this.selectedListId) {
            if (!this.stompClient || !this.connected || !categoryId) {
                console.log('WebSocket не готов или список не выбран');
                return;
            }

            const message = {
                categoryId: categoryId,
                blockKey: blockKey,
                payload: JSON.stringify({ action: 'refresh_items' })
            };

            console.log(`📤 Категория ${categoryId}:`, message);
            this.stompClient.send(`/app/category.update.${categoryId}`, {}, JSON.stringify(message));
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
            this.togglingItems[itemId] = true;  // Loading ON

            try {
                const res = await fetch(`/items/${itemId}/toggle?userId=${this.currentUser_id}`, {
                    method: 'POST'
                });
                if (!res.ok) {
                    throw new Error('Ошибка при изменении статуса');
                }
                await this.loadListItems(this.selectedListId);  // Обновляем с сервера
                this.sendCategoryUpdate('items-refresh'); // Оповещаем всех в ЭТОЙ категории (списке)
            } catch (err) {
                alert(err.message);
            } finally {
                this.togglingItems[itemId] = false;  // Loading OFF
            }
        },
        selectList(list) {
            this.selectedListId = list.id;
            this.currentView = 'check';

            // Отписываемся от предыдущей категории (если была)
            if (this.currentSubscription) {
                this.currentSubscription.unsubscribe();
            }

            // Подписываемся на обновления ЭТОГО списка
            this.currentSubscription = this.stompClient.subscribe(`/topic/category.${list.id}`, (message) => {
                const update = JSON.parse(message.body);
                if (update.blockKey === 'items-refresh') {
                    console.log(`Обновляем элементы списка ${list.id}`);
                    this.loadListItems(list.id);  // Перезагружаем элементы
                }
            });

            this.loadListItems(list.id);
        },
        disconnectWebSocket() {
            if (this.stompClient) {
              this.stompClient.disconnect();
              this.connected = false;
            }
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
                // Оповестить ВСЕХ клиентов обновить списки
                this.sendGlobalListsUpdate();
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
                this.sendGlobalListsUpdate();
                this.sendCategoryUpdate('items-refresh', listData.listId);
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
                    // Оповестить ВСЕХ клиентов обновить списки
                    this.sendGlobalListsUpdate();
                } else {
                    alert('Ошибка удаления.');
                }
            } catch (e) {
                alert('Ошибка сети.');
            }
        },
        logout() {
            // 1. Удаляем куки авторизации
            this.clearAuthCookies();

            // 2. Очищаем все данные приложения
            this.clearAppData();

            // 3. Перенаправляем на страницу входа
            this.redirectToLogin();
        },
        clearAuthCookies() {
            // Основные куки авторизации, которые нужно удалить
            const authCookies = [
                'user_id',           // Основная кука (видно в коде)
                'auth_token',        // Кука из фильтра авторизации
                'refresh_token',     // Если используется
                'JSESSIONID',        // Сессия Spring
                'XSRF-TOKEN'         // CSRF токен
            ];

            // Удаляем каждую куку
            authCookies.forEach(cookieName => {
                // Устанавливаем срок жизни в прошлое
                document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;

                // Для localhost нужно без domain
                document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
            });

            // Также удаляем все остальные куки
            document.cookie.split(';').forEach(cookie => {
                const name = cookie.split('=')[0].trim();
                if (name) {
                    document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
                }
            });
        },
        clearAppData() {
            // Сбрасываем все данные Vue приложения
            this.currentUser_id = null;
            this.selectedListId = null;
            this.lists = [];
            this.wishlistItems = [];
            this.currentView = 'check';
            this.editingList = null;
            this.togglingItems = {};

            // Очищаем локальное хранилище
            if (typeof localStorage !== 'undefined') {
                localStorage.clear();
            }
            if (typeof sessionStorage !== 'undefined') {
                sessionStorage.clear();
            }
        },
        redirectToLogin() {
            // отправляем запрос на сервер для завершения сессии
            fetch('/auth/logout', {
                method: 'POST',
                credentials: 'include'  // Отправляем куки
            }).catch(() => {
                // Игнорируем ошибки, если эндпоинта нет
            }).finally(() => {
                // Показываем сообщение
                //alert('Вы успешно вышли из системы.');

                // Переходим на главную страницу (которая должна быть страницей входа)
                window.location.href = '/';

                // Принудительно перезагружаем, чтобы серверная авторизация сработала
                setTimeout(() => {
                    window.location.reload();
                }, 100);
            });
        }
    },
    mounted() {
        this.loadCurrentUser();
        this.loadLists();
        this.connectWebSocket();
    },
    beforeUnmount() {
        if (this.currentSubscription) {
                this.currentSubscription.unsubscribe();
            }
        this.disconnectWebSocket();
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