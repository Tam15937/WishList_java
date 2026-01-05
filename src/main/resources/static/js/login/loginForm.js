const LoginForm = {
    emits: ['registration-success'],
    data() {
        return {
            username: '',
            password: '',
            loading: false,
            error: ''
        }
    },
    methods: {
        async login() {
            // Валидация
            if (!this.username.trim() || !this.password.trim()) {
                this.error = 'Заполните все поля';
                return;
            }

            this.loading = true;
            this.error = '';

            try {
                const response = await fetch('/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        name: this.username,
                        password: this.password
                    })
                });

                if (response.ok) {
                    window.location.href = '/';
                } else {
                    const errorData = await response.json();
                    this.error = errorData.error || 'Ошибка входа. Проверьте имя пользователя и пароль.';
                }
            } catch (err) {
                console.error('Login error:', err);
                this.error = 'Ошибка сети. Попробуйте еще раз.';
            } finally {
                this.loading = false;
            }
        }
    },
    mounted() {
        this.$nextTick(() => {
            const usernameInput = this.$el.querySelector('input[type="text"]');
            if (usernameInput) {
                usernameInput.focus();
            }
        });
    },
    template: `
        <form id="loginForm" @submit.prevent="login">
            <h2>Вход в систему</h2>
            <input type="text" v-model="username" placeholder="Имя пользователя" required>
            <input type="password" v-model="password" placeholder="Пароль" required>
            <button type="submit" :disabled="loading">
                <span v-if="!loading">Войти</span>
                <span v-else>Вход...</span>
            </button>
            <div id="errorMsg" class="error-msg" v-if="error">{{ error }}</div>
        </form>
    `
};