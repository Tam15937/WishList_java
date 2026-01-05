const CheckListForm = {
    props: ['selectedList', 'wishlistItems', 'currentUser_id'],
    emits: ['toggle-item'],
    methods: {
        toggleItem(id) {
            this.$emit('toggle-item', id);
        },
        isValidLink(link) {
            return link && /^https?:\/\//i.test(link.trim());
        }
    },
    template: `
        <div id="wishlistContent">
            <div v-if="selectedList">
                <h2>Wishlist: {{ selectedList.name }}</h2>
                <ul>
                    <li v-for="item in wishlistItems" :key="item.id">
                        <label style="display: flex; align-items: center;">
                            <input
                                type="checkbox"
                                :checked="item.taken"
                                :disabled="$parent.togglingItems?.[item.id]"
                                @click.prevent="toggleItem(item.id)"
                                style="transform: scale(1.4); margin-right: 10px;"
                            />
                            <span :class="{ taken: item.taken }">{{ item.name }}</span>
                        </label>
                        <div v-if="isValidLink(item.link)" style="margin-left: 30px; margin-top: 4px;">
                            <a
                                :href="item.link"
                                target="_blank"
                                rel="noopener noreferrer"
                                style="padding: 4px 8px; border: 1px solid #2196f3;
                                       border-radius: 4px; font-size: 0.9em; color: #2196f3;
                                       text-decoration: none; display: inline-block;">
                                ссылка
                            </a>
                        </div>
                    </li>
                </ul>
            </div>
            <p v-else>Выберите пользователя слева, чтобы увидеть его wishlist.</p>
        </div>
    `
};
