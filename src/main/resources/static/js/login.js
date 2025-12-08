// Создаем отдельное Vue приложение для формы входа
const loginApp = Vue.createApp({
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
                    // Успешный вход - перенаправляем на главную
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
        // Фокус на поле ввода имени пользователя
        this.$nextTick(() => {
            const usernameInput = document.querySelector('#loginForm input[type="text"]');
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
            <div id="errorMsg" v-if="error">{{ error }}</div>
        </form>
    `
});

// Монтируем в контейнер формы
loginApp.mount('#form-container');