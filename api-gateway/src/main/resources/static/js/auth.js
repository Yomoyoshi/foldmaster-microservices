// auth.js – управление сессией пользователя

const STORAGE_KEY = 'foldmaster_user';
let currentUser = null;

function loadUserFromStorage() {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
        try {
            currentUser = JSON.parse(stored);
        } catch(e) {
            console.error('Ошибка парсинга пользователя из localStorage', e);
        }
    }
    return currentUser;
}

function saveUserToStorage(userData) {
    currentUser = {
        id: userData.id,
        username: userData.username,
        phone: userData.phone,
        email: userData.email,
        role: userData.role,
        token: userData.token
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(currentUser));
}

function clearUser() {
    currentUser = null;
    localStorage.removeItem(STORAGE_KEY);
}

function getCurrentUser() {
    if (!currentUser) loadUserFromStorage();
    return currentUser;
}

async function login(phone, password) {
    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone, password })
    });

    // Если ошибка авторизации (401 или 404) – показываем общее сообщение
    if (response.status === 401 || response.status === 404) {
        const err = await response.json();
        throw new Error(err.message || 'Неверный логин или пароль');
    }

    if (!response.ok) {
        const err = await response.json();
        throw new Error(err.message || 'Ошибка входа');
    }

    const result = await response.json();
    const userData = result.data;
    saveUserToStorage(userData);
    return userData;
}

async function register(phone, name, email, password) {
    console.log('Register request:', { phone, name, email, password });
    const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            phone: phone,
            username: name,
            email: email,
            password: password
        })
    });

    if (response.status === 409) {
        const err = await response.json();
        throw new Error(err.message || 'Пользователь с таким номером уже существует');
    }

    if (!response.ok) {
        const err = await response.json();
        throw new Error(err.message || 'Ошибка регистрации');
    }

    const result = await response.json();
    return result.data;
}

function logout() {
    clearUser();
    window.location.reload();
}

function updateNavAuth() {
    const user = getCurrentUser();
    const authLink = document.getElementById('authNavLink');
    const adminLink = document.getElementById('adminNavLink');

    if (user) {
        authLink.innerHTML = 'Выйти';
        authLink.onclick = (e) => {
            e.preventDefault();
            if (confirm('Выйти из аккаунта?')) {
                logout();
            }
        };
        if (user.role === 'ADMIN' && adminLink) {
            adminLink.style.display = 'inline';
        } else if (adminLink) {
            adminLink.style.display = 'none';
        }
    } else {
        authLink.innerHTML = 'Войти';
        authLink.onclick = (e) => {
            e.preventDefault();
            showAuthModal();
        };
        if (adminLink) {
            adminLink.style.display = 'none';
        }
    }
}

function showAuthModal() {
    const modal = document.getElementById('authModal');
    if (!modal) {
        console.warn('authModal не найден на этой странице');
        return;
    }

    // Показываем шаг входа, скрываем регистрацию
    document.getElementById('authStepLogin').style.display = 'block';
    document.getElementById('authStepRegister').style.display = 'none';

    // Очищаем поля и ошибки
    document.getElementById('loginPhone').value = '';
    document.getElementById('loginPassword').value = '';
    document.getElementById('authError').innerText = '';
    document.getElementById('registerError').innerText = '';

    modal.style.display = 'flex';
}

function closeAuthModal() {
    const modal = document.getElementById('authModal');
    if (modal) modal.style.display = 'none';
}

function initAuthModal() {
    const loginBtn = document.getElementById('loginBtn');
    const phoneField = document.getElementById('loginPhone');
    const passwordField = document.getElementById('loginPassword');
    const errorDiv = document.getElementById('authError');

    const registerConfirmBtn = document.getElementById('registerConfirmBtn');
    const registerErrorDiv = document.getElementById('registerError');

    const showRegisterLink = document.getElementById('showRegisterLink');
    const showLoginLink = document.getElementById('showLoginLink');

    const forgotLink = document.getElementById('forgotPasswordLink');
    const forgotBtn = document.getElementById('forgotPasswordBtn');
    const backToLoginLink = document.getElementById('backToLoginLink');

    const closeSpan = document.querySelector('.close-auth');

    // ===== Вход =====
    if (loginBtn && phoneField && passwordField && errorDiv) {
        loginBtn.onclick = async () => {
            const phone = phoneField.value.trim();
            const password = passwordField.value.trim();
            if (!phone || !password) {
                errorDiv.innerText = 'Заполните все поля';
                return;
            }
            try {
                const user = await login(phone, password);
                closeAuthModal();
                updateNavAuth();
                window.location.reload();
            } catch (err) {
                errorDiv.innerText = err.message; // "Неверный логин или пароль"
            }
        };
    }

    // ===== Регистрация =====
    if (registerConfirmBtn && registerErrorDiv) {
        registerConfirmBtn.onclick = async () => {
            const phone = document.getElementById('registerPhone').value.trim();
            const name = document.getElementById('registerName').value.trim();
            const email = document.getElementById('registerEmail').value.trim();
            const password = document.getElementById('registerPassword').value.trim();
            if (!phone || !name || !email || !password) {
                registerErrorDiv.innerText = 'Заполните все поля';
                return;
            }
            try {
                const user = await register(phone, name, email, password);
                closeAuthModal();
                updateNavAuth();
                window.location.reload();
            } catch (err) {
                registerErrorDiv.innerText = err.message;
            }
        };
    }

    // ===== ССЫЛКА "ЗАБЫЛИ ПАРОЛЬ?" =====
    if (forgotLink) {
        forgotLink.onclick = (e) => {
            e.preventDefault();
            showForgotPasswordStep();
        };
    }

    // ===== КНОПКА ОТПРАВКИ ЗАПРОСА НА ВОССТАНОВЛЕНИЕ =====
    if (forgotBtn) {
        forgotBtn.onclick = async () => {
            const phone = document.getElementById('forgotPhone').value.trim();
            if (!phone) {
                document.getElementById('forgotError').innerText = 'Введите номер телефона';
                return;
            }
            await requestPasswordReset(phone);
        };
    }

    // ===== ССЫЛКА "ВЕРНУТЬСЯ КО ВХОДУ" =====
    if (backToLoginLink) {
        backToLoginLink.onclick = (e) => {
            e.preventDefault();
            document.getElementById('authStepForgot').style.display = 'none';
            document.getElementById('authStepLogin').style.display = 'block';
            document.getElementById('forgotError').innerText = '';
        };
    }

    // ===== Переключение между шагами =====
    if (showRegisterLink) {
        showRegisterLink.onclick = (e) => {
            e.preventDefault();
            document.getElementById('authStepLogin').style.display = 'none';
            document.getElementById('authStepRegister').style.display = 'block';
            document.getElementById('authError').innerText = '';
            document.getElementById('registerError').innerText = '';
        };
    }

    if (showLoginLink) {
        showLoginLink.onclick = (e) => {
            e.preventDefault();
            document.getElementById('authStepLogin').style.display = 'block';
            document.getElementById('authStepRegister').style.display = 'none';
            document.getElementById('authError').innerText = '';
            document.getElementById('registerError').innerText = '';
        };
    }

    // ===== Закрытие модалки =====
    if (closeSpan) {
        closeSpan.onclick = closeAuthModal;
    }

    window.addEventListener('click', function(event) {
        const modal = document.getElementById('authModal');
        if (event.target === modal) closeAuthModal();
    });
}

function showForgotPasswordStep() {
    document.getElementById('authStepLogin').style.display = 'none';
    document.getElementById('authStepRegister').style.display = 'none';
    document.getElementById('authStepForgot').style.display = 'block';
    document.getElementById('authError').innerText = '';
    document.getElementById('forgotError').innerText = '';
    document.getElementById('forgotPhone').value = '';
}

async function requestPasswordReset(phone) {
    try {
        const response = await fetch('/api/auth/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phone })
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Ошибка отправки');
        }
        const result = await response.json();
        alert(result.message || 'Ссылка отправлена, если номер зарегистрирован.');

        document.getElementById('authStepForgot').style.display = 'none';
        document.getElementById('authStepLogin').style.display = 'block';
    } catch (err) {
        document.getElementById('forgotError').innerText = err.message;
    }
}

// ===== ФУНКЦИЯ СБРОСА ПАРОЛЯ =====
async function resetPassword(token, newPassword, confirmPassword) {
    if (newPassword !== confirmPassword) {
        throw new Error('Пароли не совпадают');
    }
    if (newPassword.length < 6) {
        throw new Error('Пароль должен быть не менее 6 символов');
    }
    const response = await fetch('/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword })
    });
    if (!response.ok) {
        const err = await response.json();
        throw new Error(err.message || 'Ошибка сброса пароля');
    }
    return await response.json();
}

// ===== Глобальный экспорт функций =====
window.resetPassword = resetPassword;
window.showAuthModal = showAuthModal;
window.updateNavAuth = updateNavAuth;
window.initAuthModal = initAuthModal;
window.logout = logout;
window.closeAuthModal = closeAuthModal;
window.requestPasswordReset = requestPasswordReset;
window.showForgotPasswordStep = showForgotPasswordStep;

// ===== Инициализация при загрузке DOM =====
document.addEventListener('DOMContentLoaded', () => {
    updateNavAuth();
    initAuthModal();
});