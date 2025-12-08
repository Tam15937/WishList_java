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
            // 1. Очищаем контейнер и размонтируем предыдущее Vue приложение
            const container = document.getElementById('form-container');

            // Проверяем, есть ли смонтированное Vue приложение
            if (container._vueApp) {
                container._vueApp.unmount();
                container._vueApp = null;
            }

            container.innerHTML = '';

            // 2. Создаем новый скрипт
            const script = document.createElement('script');

            if (formType === 'login') {
                script.src = 'js/login.js?v=' + Date.now(); // Добавляем версию для избежания кэша
            } else {
                script.src = 'js/register.js?v=' + Date.now();
            }

            // 3. Удаляем предыдущие скрипты форм
            const oldScripts = document.querySelectorAll('script[src*="login.js"], script[src*="register.js"]');
            oldScripts.forEach(oldScript => {
                if (oldScript !== script && oldScript.parentNode) {
                    oldScript.parentNode.removeChild(oldScript);
                }
            });

            // 4. Добавляем новый скрипт
            script.onload = function() {
                console.log('Форма загружена:', formType);
            };

            script.onerror = function() {
                console.error('Ошибка загрузки формы:', formType);
            };

            document.body.appendChild(script);
        }
    },
    mounted() {
        // Загружаем форму входа по умолчанию
        this.loadForm('login');
    }
}).mount('#app');