const RegisterForm = {
    emits: ['registration-success'],
    data() {
        return {
            username: '',
            password: '',
            confirmPassword: '',
            loading: false,
            error: ''
        }
    },
    methods: {
        async register() {
            // Валидация
            if (!this.username.trim() || !this.password.trim() || !this.confirmPassword.trim()) {
                this.error = 'Заполните все поля';
                return;
            }

            this.loading = true;
            this.error = '';

            try {
                const response = await fetch('/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        name: this.username.trim(),
                        password: this.password.trim(),
                        confirmPassword: this.confirmPassword.trim()
                    }),
                    credentials: 'include' // Важно для отправки/получения кук
                });

                if (response.ok) {
                    // Парсим ответ с токеном
                    const data = await response.json();
                    console.log('Registration success:', data);

                    // 1. Устанавливаем куку user_id вручную (если сервер ее не установил)
                    this.setUserIdCookie(data.userId);

                    // 2. Устанавливаем куку auth_token вручную (если сервер ее не установил)
                    this.setAuthTokenCookie(data.token);

                    // 3. Перенаправляем на главную страницу
                    setTimeout(() => {
                        window.location.href = '/';
                    }, 100);

                } else {
                    const errorData = await response.json();
                    this.error = errorData.error || errorData.message || 'Ошибка регистрации';
                }
            } catch (err) {
                console.error('Register error:', err);
                this.error = 'Ошибка сети. Попробуйте еще раз.';
            } finally {
                this.loading = false;
            }
        },

        // Устанавливаем куку user_id
        setUserIdCookie(userId) {
            const expires = new Date();
            expires.setTime(expires.getTime() + (7 * 24 * 60 * 60 * 1000)); // 7 дней

            document.cookie = `user_id=${userId}; expires=${expires.toUTCString()}; path=/`;
        },

        // Устанавливаем куку auth_token
        setAuthTokenCookie(token) {
            const expires = new Date();
            expires.setTime(expires.getTime() + (7 * 24 * 60 * 60 * 1000)); // 7 дней

            document.cookie = `auth_token=${token}; expires=${expires.toUTCString()}; path=/`;
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
        <form id="registerForm" @submit.prevent="register">
            <h2>Регистрация</h2>
            <input type="text" v-model="username" @input="username = username.replace(/[^a-zA-Z0-9а-яА-я]/g, '')" placeholder="Имя пользователя" required>
            <input type="password" v-model="password" placeholder="Пароль" required>
            <input type="password" v-model="confirmPassword" placeholder="Подтвердите пароль" required>
            <button type="submit" :disabled="loading">
                <span v-if="!loading">Зарегистрироваться</span>
                <span v-else>Регистрация...</span>
            </button>
            <div id="errorMsg" class="error-msg" v-if="error">{{ error }}</div>
        </form>
    `
};