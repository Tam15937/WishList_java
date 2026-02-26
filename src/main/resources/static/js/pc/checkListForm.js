const CheckListForm = {
    props: ['selectedList', 'wishlistItems', 'currentUser_id'],
    emits: ['toggle-item'],
    methods: {
        toggleItem(itemId) {
            this.$emit('toggle-item', itemId);
        },
        isValidLink(link) {
            return link && /^https?:\/\//i.test(link.trim());
        }
    },
    template: `
        <div class="wishlist-content">
            <div v-if="selectedList">
                <h2>Wishlist: {{ selectedList.name }}</h2>
                <ul class="wishlist-items">
                    <li v-for="item in wishlistItems" :key="item.id" class="wishlist-item" :class="{ 'my-taken-item': item.isMine }">
                        <!-- ОСНОВНОЙ КОНТЕНТ li -->
                        <div style="display: flex; align-items: center; flex-grow: 1;">
                            <input type="checkbox"
                                   :checked="item.taken"
                                   :disabled="$parent.togglingItems?.[item.id]"
                                   @click.prevent="toggleItem(item.id)"
                                   style="margin-right: 12px;" />

                            <span :class="{ taken: item.taken }" style="flex-grow: 1; margin-left: 8px;">
                                {{ item.name }}
                            </span>

                            <a v-if="isValidLink(item.link)"
                               :href="item.link"
                               target="_blank"
                               rel="noopener noreferrer"
                               style="margin-left: 12px; padding: 2px 6px; border: 1px solid #2196f3; border-radius: 3px; font-size: 0.9em; color: #2196f3; text-decoration: none;">
                                ссылка
                            </a>
                        </div>
                        <!-- ИНДИКАТОР СПРАВА от li -->
                        <span v-if="$parent.togglingItems?.[item.id]"
                              style="display: inline-block; width: 20px; text-align: center; font-size: 0.8em; color: #2196f3;">
                            ⏳
                        </span>
                    </li>
                </ul>
            </div>
            <p v-else>Выберите пользователя слева, чтобы увидеть его wishlist.</p>
        </div>
    `
};
