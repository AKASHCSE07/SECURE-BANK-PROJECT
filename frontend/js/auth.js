/**
 * SECUREBANK AUTHENTICATION CONTROLLER (auth.js)
 * 
 * Handles:
 * 1. Customer registration & Success Confirmation view.
 * 2. Customer & Admin login workflows (Real Spring Boot JWT + Seamless Simulation Engine).
 * 3. Session persistence & Route Guard protection.
 * 4. Safe logout.
 */

// -----------------------------------------------------------------------------
// 1. REGISTRATION HANDLER
// -----------------------------------------------------------------------------
async function handleRegisterSubmit(event) {
  event.preventDefault();
  const btn = document.getElementById('registerBtn');
  btn.disabled = true;
  btn.innerText = 'Creating Account...';

  const payload = {
    firstName: document.getElementById('firstName').value.trim(),
    lastName: document.getElementById('lastName').value.trim(),
    email: document.getElementById('email').value.trim(),
    password: document.getElementById('password').value,
    pin: document.getElementById('pin').value.trim(),
    phone: document.getElementById('phone').value.trim(),
    address: document.getElementById('address').value.trim(),
  };

  try {
    const result = await API.auth.register(payload);
    const customer = (result && result.data) || {};

    const regCard = document.getElementById('registerCard');
    const successCard = document.getElementById('successCard');

    if (regCard && successCard) {
      document.getElementById('successFirstName').innerText = customer.firstName || payload.firstName;
      document.getElementById('successEmail').innerText = customer.email || payload.email;
      document.getElementById('successAccNo').innerText = customer.accountNumber || 'SB-1001-SAV';

      regCard.style.display = 'none';
      successCard.style.display = 'block';
      window.scrollTo({ top: 0, behavior: 'smooth' });
      showToast('✓ Account created successfully!', 'success');
    } else {
      showToast('Account created successfully! Redirecting to login...', 'success');
      setTimeout(() => {
        window.location.href = 'login.html';
      }, 1200);
    }
  } catch (error) {
    showToast(error.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerText = 'Complete Account Opening';
  }
}

// -----------------------------------------------------------------------------
// 2. LOGIN HANDLER (Customer & Admin)
// -----------------------------------------------------------------------------
async function handleLoginSubmit(event) {
  event.preventDefault();
  const btn = document.getElementById('loginBtn');
  btn.disabled = true;
  btn.innerText = 'Authenticating...';

  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;

  try {
    let result;
    if (typeof currentRole !== 'undefined' && currentRole === 'ADMIN') {
      result = await API.auth.adminLogin({ email, password });
    } else {
      result = await API.auth.login({ email, password });
    }

    const authData = (result && result.data) || null;
    if (!authData || !authData.accessToken) {
      throw new Error('Authentication failed: invalid server response.');
    }

    // Persist Session
    localStorage.setItem('securebank_token', authData.accessToken);
    localStorage.setItem('securebank_user', JSON.stringify({
      userId: authData.userId,
      email: authData.email,
      fullName: authData.fullName,
      firstName: authData.firstName || (authData.fullName ? authData.fullName.split(' ')[0] : 'Customer'),
      lastName: authData.lastName || '',
      phone: authData.phone || '',
      address: authData.address || '',
      accountNumber: authData.accountNumber || '',
      role: authData.role
    }));

    showToast('Authentication granted. Welcome back!', 'success');

    setTimeout(() => {
      if (authData.role === 'ROLE_ADMIN') {
        window.location.href = 'admin-dashboard.html';
      } else {
        window.location.href = 'customer-dashboard.html';
      }
    }, 600);

  } catch (error) {
    showToast(error.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerText = 'Secure Sign In';
  }
}

// -----------------------------------------------------------------------------
// 3. SESSION GUARDS & ROUTE PROTECTION
// -----------------------------------------------------------------------------
function getCurrentUser() {
  const userJson = localStorage.getItem('securebank_user');
  if (userJson) {
    try { return JSON.parse(userJson); } catch (e) {}
  }
  return null;
}

function requireAuth(allowedRole = null) {
  const token = localStorage.getItem('securebank_token');
  const user = getCurrentUser();

  if (!token || !user) {
    window.location.href = 'login.html';
    return null;
  }

  if (allowedRole && user.role !== allowedRole) {
    showToast('Access denied: Unauthorized role.', 'error');
    window.location.href = user.role === 'ROLE_ADMIN' ? 'admin-dashboard.html' : 'customer-dashboard.html';
    return null;
  }

  return user;
}

// -----------------------------------------------------------------------------
// 4. LOGOUT HANDLER
// -----------------------------------------------------------------------------
function handleLogout() {
  localStorage.removeItem('securebank_token');
  localStorage.removeItem('securebank_user');
  window.location.href = 'login.html';
}
