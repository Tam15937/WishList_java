document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm');
    const errorMsg = document.getElementById('errorMsg');

    form.onsubmit = async (e) => {
        e.preventDefault();

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        errorMsg.textContent = '';

        if (!username || !password) {
            errorMsg.textContent = 'Введите имя и пароль!';
            return;
        }

        try {
            const response = await fetch('/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                errorMsg.textContent = errorData.error || 'Ошибка входа';
                return;
            }

            const data = await response.json();
            if (data.token) {
                localStorage.setItem('authToken', data.token);
                // Можно сохранить username для отображения в UI, например
                localStorage.setItem('username', username);
                window.location.href = '/';
            } else {
                errorMsg.textContent = 'Ошибка: токен не получен';
            }
        } catch (error) {
            console.error('Ошибка при запросе на логин:', error);
            errorMsg.textContent = 'Ошибка соединения с сервером';
        }
    };
});
