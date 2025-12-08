const { createApp } = Vue;

createApp({
    data() {
        return {
            activeForm: 'login' // 'login' или 'register'
        }
    },
    methods: {
        switchToLogin() {
            this.activeForm = 'login';
            this.loadForm('login');
        },

        switchToRegister() {
            this.activeForm = 'register';
            this.loadForm('register');
        },

        loadForm(formType) {
            // Очищаем контейнер
            const container = document.getElementById('form-container');
            container.innerHTML = '';

            // Создаем новый скрипт
            const script = document.createElement('script');

            if (formType === 'login') {
                script.src = 'js/login.js';
            } else {
                script.src = 'js/register.js';
            }

            // Удаляем предыдущие скрипты форм
            const oldScripts = document.querySelectorAll('script[src*="login.js"], script[src*="register.js"]');
            oldScripts.forEach(oldScript => {
                if (oldScript !== script && oldScript.parentNode) {
                    oldScript.parentNode.removeChild(oldScript);
                }
            });

            // Добавляем новый скрипт
            document.body.appendChild(script);
        }
    },
    mounted() {
        // Загружаем форму входа по умолчанию
        this.loadForm('login');
    }
}).mount('#app');