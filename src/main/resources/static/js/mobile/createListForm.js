const CreateListForm = {
    props: ['currentUser_id'],
    emits: ['create-list', 'cancel-create'],
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
        removeGift(index) {
            if (this.gifts.length > 1) this.gifts.splice(index, 1);
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
            const name = this.newListName.trim();
            if (!name) {
                alert("Введите название списка!");
                return;
            }

            const gifts = this.gifts
                .filter(g => g.name.trim())
                .map(g => ({ name: g.name.trim(), taken: false, link: g.link.trim() }));

            if (gifts.length === 0) {
                alert("Добавьте хотя бы один подарок!");
                return;
            }
            if (!this.validateGifts()) return;

            this.$emit('create-list', { name, gifts });
        },
        cancelCreate() {
            this.$emit('cancel-create');
        }
    },


    template: `
        <form id="createListForm" @submit.prevent="submitForm">
            <input type="text" v-model="newListName" placeholder="Название списка" maxlength="30" required />
            <div id="giftsContainer">
                <div v-for="(gift, index) in gifts" :key="index" style="margin-bottom: 12px;">
                    <input type="text" v-model="gift.name"
                        :placeholder="'Подарок ' + (index + 1)" maxlength="40"
                        class="giftInput" />
                    <input type="text" v-model="gift.link"
                        placeholder="Ссылка (опционально)" class="giftInput" />
                    <button type="button" @click="removeGift(index)" class="remove-gift"
                        v-if="gifts.length > 1"
                        style="background-color:#f44336; color:white; margin-top:4px;">
                        Удалить
                    </button>
                </div>
            </div>
            <button type="button" class="addGiftBtn" @click="addGiftInput">
                Добавить подарок
            </button>
            <button type="submit" class="saveListBtn">Сохранить лист</button>
            <button type="button" @click="cancelCreate" style="background:#757575;">
                Отмена
            </button>
        </form>
    `
};
