const EditListForm = {
    props: ['editingList', 'currentUser_id'],
    emits: ['update-list', 'cancel-edit'],
    data() {
        return {
            newListName: '',
            gifts: [{ name: '', link: '' }]
        }
    },
    methods: {
        addGiftInput() {
            this.gifts.push({ name: '', link: '' });
        },
        removeGift(index) {
            if (this.gifts.length > 1) {
                this.gifts.splice(index, 1);
            }
        },
        validateGifts() {
            for (const gift of this.gifts) {
                if (gift.link && !/^https?:\/\//i.test(gift.link.trim())) {
                    alert("Ссылка должна начинаться с http:// или https://");
                    return false;
                }
            }
            return true;
        },
        submitForm() {
            const listName = this.newListName.trim();
            if (!listName) {
                alert("Введите название списка!");
                return;
            }

            const validGifts = this.gifts
                .filter(gift => gift.name.trim())
                .map(gift => ({
                    name: gift.name.trim(),
                    link: gift.link.trim()
                }));

            if (validGifts.length === 0) {
                alert("Добавьте хотя бы один подарок!");
                return;
            }

            if (!this.validateGifts()) return;

            this.$emit('update-list', {
                listId: this.editingList.id,
                name: listName,
                gifts: validGifts
            });
        },
        cancelEdit() {
            this.$emit('cancel-edit');
        },
        loadListItems() {
            fetch(`/items/list/${this.editingList.id}`)
                .then(r => r.json())
                .then(items => {
                    this.gifts = items.map(item => ({
                        name: item.name,
                        link: item.link || ''
                    }));
                    if (this.gifts.length === 0) {
                        this.gifts = [{ name: '', link: '' }];
                    }
                })
                .catch(() => alert('Ошибка загрузки подарков'));
        }
    },
    mounted() {
        this.newListName = this.editingList.name;
        this.loadListItems();
    },
    template: `
        <div class="wishlist-content">
            <form class="list-form" @submit.prevent="submitForm">
                <input type="text" v-model="newListName" placeholder="Название списка" maxlength="30" required />
                <div class="gifts-container">
                    <div v-for="(gift, index) in gifts" :key="index" class="gift-block">
                        <input type="text" v-model="gift.name" :placeholder="'Подарок ' + (index + 1)" maxlength="40" class="gift-input" />
                        <input type="text" v-model="gift.link" placeholder="Ссылка (опционально)" class="gift-input" />
                        <button type="button" @click="removeGift(index)" class="remove-gift" v-if="gifts.length > 1">×</button>
                    </div>
                </div>
                <button type="button" @click="addGiftInput" class="add-gift-btn">Добавить подарок</button>
                <div style="display: flex; gap: 10px;">
                    <button type="submit" class="save-list-btn">Сохранить изменения</button>
                    <button type="button" @click="cancelEdit" class="cancel-btn" style="background: #757575;">Отмена</button>
                </div>
            </form>
        </div>
    `
};