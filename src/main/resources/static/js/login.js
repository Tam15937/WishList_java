const { createApp } = Vue;

createApp({
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
            this.loading = true;
            this.error = '';
            
            try {
                // Используем прямой fetch для логина (без токена)
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
                    const data = await response.json();

                    // Сохраняем токен в API клиенте
                    apiClient.setAuthToken(data.authToken);

                    // Перенаправляем на главную
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
            const usernameInput = document.querySelector('input[type="text"]');
            if (usernameInput) {
                usernameInput.focus();
            }
        });
    }
}).mount('#app');