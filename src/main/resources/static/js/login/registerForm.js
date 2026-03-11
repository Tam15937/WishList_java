const RegisterForm = {
    emits: ['registration-success'],
    data() {
        return {
            username: '',
            password: '',
            passwordVisible: false,
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
                    window.location.href = '/';
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
            <div class="password-wrapper">
                <input :type="passwordVisible ? 'text' : 'password'" v-model="password" placeholder="Пароль" required>
                <button type="button" class="toggle-password" @click="passwordVisible = !passwordVisible" @mousedown.prevent tabindex="-1">
                    <span v-if="passwordVisible" class="status-dot">🟢</span>
                    <span v-else class="status-dot">🔴</span>
                </button>
            </div>
            <input :type="passwordVisible ? 'text' : 'password'" v-model="confirmPassword" placeholder="Подтвердите пароль" required>
            <button type="submit" :disabled="loading">
                <span v-if="!loading">Зарегистрироваться</span>
                <span v-else>Регистрация...</span>
            </button>
            <div id="errorMsg" class="error-msg" v-if="error">{{ error }}</div>
        </form>
    `
};