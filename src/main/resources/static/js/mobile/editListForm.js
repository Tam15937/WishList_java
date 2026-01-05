const EditListForm = {
    props: ['editingList', 'currentUser_id'],
    emits: ['update-list', 'cancel-edit'],
    data() {
        return {
            newListName: '',
            gifts: [{ name: '', link: '' }]
        };
    },
    methods: {
        addGiftInput() {
            this.gifts.push({ name: '', link: '' });
        },
        removeGift(i) {
            if (this.gifts.length > 1) this.gifts.splice(i, 1);
        },
        validateGifts() {
            for (const gift of this.gifts) {
                if (gift.link && !/^https?:\/\//i.test(gift.link.trim())) {
                    alert('Ссылка должна начинаться с http:// или https://');
                    return false;
                }
            }
            return true;
        },
        submitForm() {
            const name = this.newListName.trim();
            if (!name) {
                alert('Введите название списка!');
                return;
            }

            const gifts = this.gifts
                .filter(g => g.name.trim())
                .map(g => ({ name: g.name.trim(), link: g.link.trim() }));

            if (gifts.length === 0) {
                alert('Добавьте хотя бы один подарок!');
                return;
            }
            if (!this.validateGifts()) return;

            this.$emit('update-list', {
                listId: this.editingList.id,
                name,
                gifts
            });
        },
        cancelEdit() {
            this.$emit('cancel-edit');
        },
        loadListItems() {
            fetch(`/items/list/${this.editingList.id}`)
                .then(r => r.json())
                .then(items => {
                    this.gifts = items.map(i => ({ name: i.name, link: i.link || '' }));
                    if (this.gifts.length === 0) this.gifts = [{ name: '', link: '' }];
                })
                .catch(() => alert('Ошибка загрузки подарков'));
        }
    },
    mounted() {
        this.newListName = this.editingList.name;
        this.loadListItems();
    },
    template: `
        <form id="editListForm" @submit.prevent="submitForm">
            <input
                type="text"
                v-model="newListName"
                placeholder="Название списка"
                maxlength="30"
                required
            />

            <div id="giftsContainer">
                <div
                    v-for="(gift, i) in gifts"
                    :key="i"
                    class="gift-block"
                >
                    <input
                        type="text"
                        v-model="gift.name"
                        :placeholder="'Подарок ' + (i + 1)"
                        maxlength="40"
                        class="giftInputName"
                    />

                    <input
                        type="text"
                        v-model="gift.link"
                        placeholder="Ссылка (опционально)"
                        class="giftInputLink"
                    />

                    <button
                        type="button"
                        @click="removeGift(i)"
                        class="remove-gift"
                        v-if="gifts.length > 1"
                    >
                        ×
                    </button>
                </div>
            </div>

            <button
                type="button"
                class="addGiftBtn"
                @click="addGiftInput"
            >
                Добавить подарок
            </button>
            <button type="submit" class="saveListBtn">Сохранить изменения</button>
            <button
                type="button"
                @click="cancelEdit"
                class="cancel-btn"
                style="background: #757575;"
            >
                Отмена
            </button>
        </form>
    `
};
