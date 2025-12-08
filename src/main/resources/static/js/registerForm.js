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

            if (this.username.length < 3) {
                this.error = 'Имя пользователя должно быть не менее 3 символов';
                return;
            }

            if (this.password.length < 6) {
                this.error = 'Пароль должен быть не менее 6 символов';
                return;
            }

            if (this.password !== this.confirmPassword) {
                this.error = 'Пароли не совпадают';
                return;
            }

            this.loading = true;
            this.error = '';

            try {
                // 1. Регистрация
                const registerResponse = await fetch('/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        name: this.username,
                        password: this.password,
                        confirmPassword: this.confirmPassword
                    })
                });

                if (registerResponse.ok) {
                    // 2. Автоматический вход после регистрации
                    const loginResponse = await fetch('/auth/login', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            name: this.username,
                            password: this.password
                        })
                    });

                    if (loginResponse.ok) {
                        // Успешная авторизация - перенаправляем на главную
                        window.location.href = '/';
                    } else {
                        // Если автоматический вход не удался
                        this.$emit('registration-success');
                        alert('Регистрация успешна! Теперь войдите в систему.');
                    }
                } else {
                    const errorData = await registerResponse.json();
                    this.error = errorData.error || 'Ошибка регистрации';
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
            <input type="text" v-model="username" placeholder="Имя пользователя" required>
            <input type="password" v-model="password" placeholder="Пароль" required>
            <input type="password" v-model="confirmPassword" placeholder="Подтвердите пароль" required>
            <button type="submit" :disabled="loading">
                <span v-if="!loading">Зарегистрироваться</span>
                <span v-else>Регистрация...</span>
            </button>
            <div id="errorMsg" v-if="error">{{ error }}</div>
        </form>
    `
};