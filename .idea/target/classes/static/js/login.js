/**
 * 登录页面逻辑
 * 文件：js/login.js
 * 描述：登录/注册页面的业务逻辑
 * 作者：教育AI团队
 */

let currentRole = 'STUDENT'

function switchTab(tab, element) {
    // 切换标签样式
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'))
    element.classList.add('active')

    // 显示/隐藏表单
    if (tab === 'login') {
        document.getElementById('loginForm').style.display = 'block'
        document.getElementById('registerForm').style.display = 'none'
        document.getElementById('subtitle').textContent = '请输入账号密码登录系统'
    } else {
        document.getElementById('loginForm').style.display = 'none'
        document.getElementById('registerForm').style.display = 'block'
        document.getElementById('subtitle').textContent = '创建新账号，开始使用平台'
    }

    hideMessages()
}

function selectRole(role, element) {
    currentRole = role

    // 更新选中状态
    document.querySelectorAll('.role-option').forEach(opt => opt.classList.remove('selected'))
    element.classList.add('selected')

    // 更新标签文字
    const label = document.getElementById('idLabel')
    const idInput = document.getElementById('regId')
    if (role === 'STUDENT') {
        label.textContent = '学号'
        idInput.placeholder = '请输入学号，如：S001'
    } else {
        label.textContent = '工号'
        idInput.placeholder = '请输入工号，如：T001'
    }
}

async function doLogin() {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();

    if (!username || !password) {
        showError('请输入用户名和密码');
        return;
    }

    const btn = document.getElementById('loginBtn');
    btn.disabled = true;
    btn.textContent = '登录中...';
    hideMessages();

    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await res.json();

        if (data.success && data.user) {
            localStorage.setItem('token', data.token || 'logged-in');
            localStorage.setItem('user', JSON.stringify({
                username: data.user.username || username,
                realName: data.user.realName || username,
                role: data.user.role,
                studentId: data.user.studentId,
                teacherId: data.user.teacherId,
                userId: data.user.id
            }));

            const role = data.user.role;
            if (role === 'TEACHER') {
                window.location.href = '/teacher.html';
            } else if (role === 'STUDENT') {
                window.location.href = '/student.html';
            } else {
                showError('未知角色类型');
                resetButton();
            }
        } else {
            showError(data.message || '用户名或密码错误');
            resetButton();
        }
    } catch (e) {
        console.error('登录错误:', e);
        showError('网络错误，请检查服务器是否启动');
        resetButton();
    }
}

async function doRegister() {
    const id = document.getElementById('regId').value.trim()
    const name = document.getElementById('regName').value.trim()
    const username = document.getElementById('regUsername').value.trim()
    const password = document.getElementById('regPassword').value.trim()

    // 验证
    if (!id || !name || !username || !password) {
        showError('请填写所有必填项')
        return
    }

    if (password.length < 6) {
        showError('密码长度不能少于6位')
        return
    }

    const btn = document.getElementById('regBtn')
    btn.disabled = true
    btn.textContent = '注册中...'
    hideMessages()

    try {
        const userData = {
            username: username,
            password: password,
            realName: name,
            role: currentRole
        }

        if (currentRole === 'STUDENT') {
            userData.studentId = id
        } else {
            userData.teacherId = id
        }

        const res = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        })

        const data = await res.json()

        if (data.success) {
            showSuccess('注册成功！正在跳转到登录...')
            setTimeout(() => {
                switchTab('login', document.getElementById('loginTab'))
                document.getElementById('username').value = username
                document.getElementById('password').value = ''
            }, 1500)
        } else {
            showError(data.message || '注册失败')
            resetRegButton()
        }
    } catch (e) {
        console.error('注册错误:', e)
        showError('网络错误，请检查服务器是否启动')
        resetRegButton()
    }
}

function showError(msg) {
    const el = document.getElementById('errorMsg');
    el.textContent = msg;
    el.classList.add('show');
    document.getElementById('successMsg').classList.remove('show');
    resetButton();
    resetRegButton();
}

function showSuccess(msg) {
    const el = document.getElementById('successMsg');
    el.textContent = msg;
    el.classList.add('show');
    document.getElementById('errorMsg').classList.remove('show');
}

function hideMessages() {
    document.getElementById('errorMsg').classList.remove('show')
    document.getElementById('successMsg').classList.remove('show')
}

function resetButton() {
    const btn = document.getElementById('loginBtn');
    btn.disabled = false;
    btn.textContent = '登 录';
}

function resetRegButton() {
    const btn = document.getElementById('regBtn');
    btn.disabled = false;
    btn.textContent = '注 册';
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('password').addEventListener('keydown', e => {
        if (e.key === 'Enter') doLogin();
    });
    document.getElementById('regPassword').addEventListener('keydown', e => {
        if (e.key === 'Enter') doRegister();
    });
});
