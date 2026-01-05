const { createApp } = Vue;

const App = {
    components: {
        LoginForm,
        RegisterForm
    },
    data() {
        return {
            activeForm: 'login' // 'login' или 'register'
        }
    },
    computed: {
        currentForm() {
            return this.activeForm === 'login' ? 'LoginForm' : 'RegisterForm';
        }
    },
    methods: {
        switchToLogin() {
            this.activeForm = 'login';
        },
        switchToRegister() {
            this.activeForm = 'register';
        },
        handleRegistrationSuccess() {
            alert('Регистрация успешна! Теперь войдите в систему.');
            this.switchToLogin();
        }
    },
    mounted() {
        // Автофокус при переключении форм
        this.$watch('activeForm', (newForm) => {
            this.$nextTick(() => {
                const input = this.$el.querySelector('input[type="text"]');
                if (input) input.focus();
            });
        });
    },
    template: `
        <div class="container">
            <div class="form-tabs">
                <button
                    class="tab-btn"
                    :class="{ 'active': activeForm === 'login' }"
                    @click="switchToLogin">
                    Вход
                </button>
                <button
                    class="tab-btn"
                    :class="{ 'active': activeForm === 'register' }"
                    @click="switchToRegister">
                    Регистрация
                </button>
            </div>

            <!-- Динамический компонент -->
            <component :is="currentForm"
                      @registration-success="handleRegistrationSuccess" />
        </div>
    `
};

createApp(App).mount('#app');