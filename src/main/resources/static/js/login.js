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
            const resp = await fetch('/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password }),
            });

            const text = await resp.text();
            console.log('Response text:', text);

            try {
                const data = JSON.parse(text);
                if (resp.ok) {
                    window.location.href = '/';
                } else {
                    errorMsg.textContent = data.error || 'Ошибка входа';
                }
            } catch (e) {
                console.error('Ошибка парсинга JSON:', e);
                errorMsg.textContent = 'Неверный ответ от сервера';
            }
           }
    };
});
