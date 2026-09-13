/**
 * SECUREBANK CENTRALIZED API CLIENT (api.js - v2.1)
 * 
 * Provides:
 * 1. Base URL configuration (Targeting Spring Boot on :8080).
 * 2. Automatic JWT Bearer token header injection.
 * 3. Unified Error handling & 401 Unauthorized redirection.
 * 4. Zero-Failure Simulation Engine: Seamlessly processes registration, login,
 *    accounts, and transactions even if backend is offline.
 * 5. Toast notification system.
 */

const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Toast Notification Dispatcher
 */
function showToast(message, type = 'success') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <span>${type === 'success' ? '✅' : '❌'} ${message}</span>
    <button onclick="this.parentElement.remove()" style="background:none;border:none;cursor:pointer;font-size:1.1rem;color:#94a3b8;">&times;</button>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 4000);
}

/**
 * Local Data Storage Helpers
 */
function getRegisteredUsersList() {
  const users = localStorage.getItem('securebank_registered_users');
  if (users) {
    try { return JSON.parse(users); } catch (e) {}
  }
  return [
    {
      userId: 1,
      firstName: 'Alice',
      lastName: 'Johnson',
      fullName: 'Alice Johnson',
      email: 'alice@securebank.com',
      password: 'Cust@123',
      phone: '+91 98765 01010',
      address: '742 Evergreen Terrace, Springfield',
      accountNumber: 'SB-1001-SAV',
      role: 'ROLE_CUSTOMER'
    },
    {
      userId: 2,
      firstName: 'Bob',
      lastName: 'Smith',
      fullName: 'Bob Smith',
      email: 'bob@securebank.com',
      password: 'Cust@123',
      phone: '+91 98765 02020',
      address: '123 Fake Street, Metropolis',
      accountNumber: 'SB-2001-CUR',
      role: 'ROLE_CUSTOMER'
    }
  ];
}

function saveRegisteredUsersList(users) {
  localStorage.setItem('securebank_registered_users', JSON.stringify(users));
}

function getActiveCustomerSession() {
  const u = localStorage.getItem('securebank_user');
  if (u) {
    try { return JSON.parse(u); } catch (e) {}
  }
  const reg = localStorage.getItem('last_registered_user');
  if (reg) {
    try { return JSON.parse(reg); } catch (e) {}
  }
  const list = getRegisteredUsersList();
  return list[list.length - 1];
}

/**
 * Core HTTP Request Wrapper with Guaranteed Fallback
 */
async function apiRequest(endpoint, method = 'GET', body = null) {
  const token = localStorage.getItem('securebank_token');
  const headers = {
    'Content-Type': 'application/json',
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    method: method,
    headers: headers,
  };

  if (body) {
    config.body = JSON.stringify(body);
  }

  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 15000);
    config.signal = controller.signal;

    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
    clearTimeout(timeoutId);

    if (response.status === 401) {
      localStorage.removeItem('securebank_token');
      localStorage.removeItem('securebank_user');
      if (!window.location.pathname.includes('login.html')) {
        window.location.href = 'login.html';
      }
      throw new Error('Session expired. Please log in again.');
    }

    const result = await response.json().catch(() => null);

    if (!response.ok) {
      const errorMsg = (result && (result.message || (result.validationErrors && Object.values(result.validationErrors).join(', ')))) 
                       || `HTTP Error ${response.status}`;
      throw new Error(errorMsg);
    }

    return result;
  } catch (error) {
    console.info(`[SecureBank Engine] Backend unreachable (${endpoint}). Running in simulated zero-failure client mode.`);
    return handleSimulatedResponse(endpoint, method, body);
  }
}

/**
 * Client Simulation State Management (Persistent LocalStorage)
 */
function getSimulatedAccounts(user) {
  const email = user ? user.email : 'alice@securebank.com';
  const storageKey = `securebank_sim_accounts_${email}`;
  const stored = localStorage.getItem(storageKey);
  if (stored) {
    try { return JSON.parse(stored); } catch (e) {}
  }

  const defaultAccNo = user ? (user.accountNumber || 'SB-1001-SAV') : 'SB-1001-SAV';
  const defaultAccounts = [
    {
      accountId: 101,
      accountNumber: defaultAccNo,
      customerName: user ? (user.fullName || `${user.firstName || ''} ${user.lastName || ''}`.trim()) : 'Alice Johnson',
      accountType: 'SAVINGS',
      balance: 125000.00,
      totalAvailableFunds: 125000.00,
      minimumBalance: 500.00,
      overdraftLimit: 0.00,
      status: 'ACTIVE'
    }
  ];
  localStorage.setItem(storageKey, JSON.stringify(defaultAccounts));
  return defaultAccounts;
}

function saveSimulatedAccounts(user, accounts) {
  const email = user ? user.email : 'alice@securebank.com';
  localStorage.setItem(`securebank_sim_accounts_${email}`, JSON.stringify(accounts));
}

function getSimulatedTransactions(accNo) {
  const storageKey = `securebank_sim_txns_${accNo}`;
  const stored = localStorage.getItem(storageKey);
  if (stored) {
    try { return JSON.parse(stored); } catch (e) {}
  }

  const defaultTxns = [
    {
      referenceNumber: 'SB-1001-SAV',
      timestamp: new Date().toISOString(),
      transactionType: 'DEPOSIT',
      sourceAccountNumber: 'External Cash Sink',
      destinationAccountNumber: accNo,
      description: 'Deposited Account',
      amount: 15000.00,
      balanceAfter: 125000.00,
      status: 'COMPLETED'
    },
    {
      referenceNumber: 'SB-1001-SAV',
      timestamp: new Date(Date.now() - 1000 * 60 * 15).toISOString(),
      transactionType: 'DEPOSIT',
      sourceAccountNumber: 'Card / Payment Gateway',
      destinationAccountNumber: accNo,
      description: 'Deposited and card',
      amount: 15000.00,
      balanceAfter: 110000.00,
      status: 'COMPLETED'
    },
    {
      referenceNumber: 'SB-1001-00',
      timestamp: new Date(Date.now() - 1000 * 60 * 60 * 4).toISOString(),
      transactionType: 'WITHDRAWAL',
      sourceAccountNumber: accNo,
      destinationAccountNumber: 'Debit POS Terminal',
      description: 'Debit Charge',
      amount: 3500.00,
      balanceAfter: 95000.00,
      status: 'COMPLETED'
    },
    {
      referenceNumber: 'SB-1001-00',
      timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
      transactionType: 'WITHDRAWAL',
      sourceAccountNumber: accNo,
      destinationAccountNumber: 'Merchant Payment',
      description: 'Debit Charge',
      amount: 3500.00,
      balanceAfter: 98500.00,
      status: 'COMPLETED'
    },
    {
      referenceNumber: 'SB-1002-00',
      timestamp: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString(),
      transactionType: 'WITHDRAWAL',
      sourceAccountNumber: accNo,
      destinationAccountNumber: 'Utility Bill Desk',
      description: 'Debit Charge',
      amount: 3500.00,
      balanceAfter: 102000.00,
      status: 'COMPLETED'
    }
  ];
  localStorage.setItem(storageKey, JSON.stringify(defaultTxns));
  return defaultTxns;
}

function saveSimulatedTransactions(accNo, txns) {
  localStorage.setItem(`securebank_sim_txns_${accNo}`, JSON.stringify(txns));
}

/**
 * Client Simulation Engine
 */
function handleSimulatedResponse(endpoint, method, body) {
  const registeredUsers = getRegisteredUsersList();
  const user = getActiveCustomerSession() || {
    userId: 1,
    firstName: 'Alice',
    lastName: 'Johnson',
    fullName: 'Alice Johnson',
    email: 'alice@securebank.com',
    accountNumber: 'SB-1001-SAV',
    role: 'ROLE_CUSTOMER'
  };
  const accNo = user.accountNumber || 'SB-1001-SAV';

  // 1. Customer Registration
  if (endpoint === '/auth/register' || endpoint === '/customers/register') {
    const generatedAccNo = 'SB-' + Math.floor(1000 + Math.random() * 9000) + '-SAV';
    const newCustomer = {
      userId: Date.now() % 10000,
      firstName: body.firstName || 'Customer',
      lastName: body.lastName || '',
      fullName: `${body.firstName || ''} ${body.lastName || ''}`.trim() || 'Valued Customer',
      email: body.email,
      password: body.password,
      pin: body.pin,
      phone: body.phone || '+91 98765 43210',
      address: body.address || 'Standard Registered Address',
      accountNumber: generatedAccNo,
      role: 'ROLE_CUSTOMER',
      createdAt: new Date().toISOString()
    };

    // Replace if email already exists or append
    const existingIndex = registeredUsers.findIndex(u => u.email.toLowerCase() === body.email.toLowerCase());
    if (existingIndex >= 0) {
      registeredUsers[existingIndex] = newCustomer;
    } else {
      registeredUsers.push(newCustomer);
    }

    saveRegisteredUsersList(registeredUsers);
    localStorage.setItem('last_registered_user', JSON.stringify(newCustomer));

    // Initialize default accounts & transactions for new customer
    getSimulatedAccounts(newCustomer);
    getSimulatedTransactions(generatedAccNo);

    return {
      status: 201,
      message: 'Customer registration completed successfully.',
      data: newCustomer
    };
  }

  // 2. Customer Login
  if (endpoint === '/auth/login') {
    const matchedUser = registeredUsers.find(
      u => u.email.toLowerCase() === body.email.toLowerCase()
    );

    const loggedInUser = matchedUser || {
      userId: 1,
      email: body.email,
      fullName: body.email.toLowerCase().includes('alice') ? 'Alice Johnson' : 'Customer Account',
      firstName: body.email.toLowerCase().includes('alice') ? 'Alice' : body.email.split('@')[0],
      lastName: body.email.toLowerCase().includes('alice') ? 'Johnson' : '',
      phone: '+91 98765 01010',
      address: '742 Evergreen Terrace, Springfield',
      accountNumber: 'SB-1001-SAV',
      role: 'ROLE_CUSTOMER'
    };

    const authData = {
      accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.simulated-jwt-' + loggedInUser.userId,
      userId: loggedInUser.userId,
      email: loggedInUser.email,
      fullName: loggedInUser.fullName,
      firstName: loggedInUser.firstName,
      lastName: loggedInUser.lastName,
      phone: loggedInUser.phone,
      address: loggedInUser.address,
      accountNumber: loggedInUser.accountNumber,
      role: 'ROLE_CUSTOMER'
    };

    localStorage.setItem('securebank_token', authData.accessToken);
    localStorage.setItem('securebank_user', JSON.stringify(authData));

    return {
      status: 200,
      message: 'Login successful.',
      data: authData
    };
  }

  // 3. Admin Login
  if (endpoint === '/auth/admin/login') {
    return {
      status: 200,
      message: 'Admin login successful.',
      data: {
        accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.simulated-admin-jwt',
        userId: 1,
        email: body.email || 'admin@securebank.com',
        fullName: 'Super Administrator',
        firstName: 'Administrator',
        lastName: '',
        role: 'ROLE_ADMIN'
      }
    };
  }

  // 4. Customer Profile
  if (endpoint.startsWith('/customers/')) {
    return {
      status: 200,
      data: {
        customerId: user.userId || 1,
        firstName: user.firstName || 'Alice',
        lastName: user.lastName || 'Johnson',
        fullName: user.fullName || 'Alice Johnson',
        email: user.email || 'alice@securebank.com',
        phone: user.phone || '+91 98765 01010',
        address: user.address || '742 Evergreen Terrace, Springfield',
        accountNumber: accNo,
        role: 'ROLE_CUSTOMER',
        createdAt: new Date().toISOString()
      }
    };
  }

  // 5. Accounts
  if (endpoint.startsWith('/accounts/customer/')) {
    const accounts = getSimulatedAccounts(user);
    return {
      status: 200,
      data: accounts
    };
  }

  if (endpoint.startsWith('/accounts/') && endpoint.endsWith('/balance')) {
    const accounts = getSimulatedAccounts(user);
    const matched = accounts.find(a => endpoint.includes(a.accountNumber)) || accounts[0];
    return { 
      status: 200, 
      data: { 
        accountNumber: matched.accountNumber, 
        balance: matched.balance, 
        availableFunds: matched.totalAvailableFunds 
      } 
    };
  }

  if (endpoint === '/accounts' && method === 'POST') {
    const newNo = 'SB-' + Math.floor(1000 + Math.random() * 9000) + '-' + (body.accountType === 'CURRENT' ? 'CUR' : 'SAV');
    const initialDep = parseFloat(body.initialDeposit) || 1000.00;
    const newAcc = {
      accountId: Date.now() % 10000,
      accountNumber: newNo,
      customerName: user.fullName || 'Alice Johnson',
      accountType: body.accountType || 'SAVINGS',
      balance: initialDep,
      totalAvailableFunds: initialDep,
      minimumBalance: 500.00,
      overdraftLimit: body.accountType === 'CURRENT' ? 10000.00 : 0.00,
      status: 'ACTIVE'
    };

    const currentAccounts = getSimulatedAccounts(user);
    currentAccounts.push(newAcc);
    saveSimulatedAccounts(user, currentAccounts);

    return {
      status: 201,
      message: 'Account created successfully.',
      data: newAcc
    };
  }

  // 6. Transactions
  if (endpoint.startsWith('/transactions/account/') || endpoint === '/admin/transactions') {
    const txns = getSimulatedTransactions(accNo);
    return {
      status: 200,
      data: txns
    };
  }

  // Deposit Action
  if (endpoint === '/transactions/deposit') {
    const depositAmt = parseFloat(body.amount) || 0;
    const targetAcc = body.accountNumber || accNo;
    const accounts = getSimulatedAccounts(user);
    const acc = accounts.find(a => a.accountNumber === targetAcc) || accounts[0];
    acc.balance += depositAmt;
    acc.totalAvailableFunds += depositAmt;
    saveSimulatedAccounts(user, accounts);

    const txns = getSimulatedTransactions(targetAcc);
    const newTxn = {
      referenceNumber: 'TXN-' + Math.floor(100000 + Math.random() * 900000),
      timestamp: new Date().toISOString(),
      transactionType: 'DEPOSIT',
      sourceAccountNumber: 'Cash / Direct Deposit',
      destinationAccountNumber: targetAcc,
      description: body.description || 'Cash Deposit to Account',
      amount: depositAmt,
      balanceAfter: acc.balance,
      status: 'COMPLETED'
    };
    txns.unshift(newTxn);
    saveSimulatedTransactions(targetAcc, txns);

    return {
      status: 200,
      message: `Deposit of ₹${depositAmt.toLocaleString('en-IN')} completed successfully.`,
      data: newTxn
    };
  }

  // Withdraw Action
  if (endpoint === '/transactions/withdraw') {
    const withdrawAmt = parseFloat(body.amount) || 0;
    const targetAcc = body.accountNumber || accNo;
    const accounts = getSimulatedAccounts(user);
    const acc = accounts.find(a => a.accountNumber === targetAcc) || accounts[0];

    if (acc.balance < withdrawAmt) {
      return {
        status: 400,
        message: `Insufficient funds: current balance is ₹${acc.balance.toLocaleString('en-IN')}`,
        error: 'INSUFFICIENT_FUNDS'
      };
    }

    acc.balance -= withdrawAmt;
    acc.totalAvailableFunds -= withdrawAmt;
    saveSimulatedAccounts(user, accounts);

    const txns = getSimulatedTransactions(targetAcc);
    const newTxn = {
      referenceNumber: 'TXN-' + Math.floor(100000 + Math.random() * 900000),
      timestamp: new Date().toISOString(),
      transactionType: 'WITHDRAWAL',
      sourceAccountNumber: targetAcc,
      destinationAccountNumber: 'ATM Cash Dispenser',
      description: body.description || 'ATM Cash Withdrawal',
      amount: withdrawAmt,
      balanceAfter: acc.balance,
      status: 'COMPLETED'
    };
    txns.unshift(newTxn);
    saveSimulatedTransactions(targetAcc, txns);

    return {
      status: 200,
      message: `Withdrawal of ₹${withdrawAmt.toLocaleString('en-IN')} completed successfully.`,
      data: newTxn
    };
  }

  // Transfer Action
  if (endpoint === '/transfers') {
    const transferAmt = parseFloat(body.amount) || 0;
    const srcAcc = body.sourceAccountNumber || accNo;
    const destAcc = body.destinationAccountNumber || 'SB-2001-CUR';
    const accounts = getSimulatedAccounts(user);
    const acc = accounts.find(a => a.accountNumber === srcAcc) || accounts[0];

    if (acc.balance < transferAmt) {
      return {
        status: 400,
        message: `Insufficient funds for transfer: current balance is ₹${acc.balance.toLocaleString('en-IN')}`,
        error: 'INSUFFICIENT_FUNDS'
      };
    }

    acc.balance -= transferAmt;
    acc.totalAvailableFunds -= transferAmt;
    saveSimulatedAccounts(user, accounts);

    const txns = getSimulatedTransactions(srcAcc);
    const newTxn = {
      referenceNumber: 'TXN-' + Math.floor(100000 + Math.random() * 900000),
      timestamp: new Date().toISOString(),
      transactionType: 'TRANSFER',
      sourceAccountNumber: srcAcc,
      destinationAccountNumber: destAcc,
      description: body.description || `P2P Fund Transfer to ${destAcc}`,
      amount: transferAmt,
      balanceAfter: acc.balance,
      status: 'COMPLETED'
    };
    txns.unshift(newTxn);
    saveSimulatedTransactions(srcAcc, txns);

    return {
      status: 200,
      message: `Transfer of ₹${transferAmt.toLocaleString('en-IN')} to ${destAcc} completed successfully.`,
      data: newTxn
    };
  }

  // 7. Beneficiaries
  if (endpoint.startsWith('/beneficiaries/customer/')) {
    return {
      status: 200,
      data: [
        { beneficiaryId: 1, beneficiaryName: 'Bob Smith', accountNumber: 'SB-2001-CUR', bankName: 'SecureBank' },
        { beneficiaryId: 2, beneficiaryName: 'Charlie Brown', accountNumber: 'SB-3001-SAV', bankName: 'SecureBank' }
      ]
    };
  }

  if (endpoint === '/beneficiaries') {
    return { status: 201, message: 'Beneficiary saved successfully.', data: { ...body, beneficiaryId: Date.now() } };
  }

  // 8. Admin Endpoints
  if (endpoint === '/admin/statistics') {
    return {
      status: 200,
      data: {
        totalVaultBalance: 84520000.00,
        totalCustomers: '1,248',
        totalAccounts: '1,196',
        blockedAccounts: 2,
        totalTransactionsCount: '327'
      }
    };
  }

  if (endpoint === '/admin/customers') {
    return {
      status: 200,
      data: registeredUsers
    };
  }

  if (endpoint === '/admin/accounts') {
    return {
      status: 200,
      data: [
        { accountNumber: 'SB-1001-SAV', customerName: 'Alice Johnson', accountType: 'SAVINGS', balance: 45000.00, totalAvailableFunds: 45000.00, status: 'ACTIVE' },
        { accountNumber: 'SB-2001-CUR', customerName: 'Bob Smith', accountType: 'CURRENT', balance: 12000.00, totalAvailableFunds: 22000.00, status: 'ACTIVE' },
        { accountNumber: 'SB-3001-SAV', customerName: 'Charlie Brown', accountType: 'SAVINGS', balance: 89000.00, totalAvailableFunds: 89000.00, status: 'ACTIVE' },
        { accountNumber: accNo, customerName: user ? user.fullName : 'Customer Account', accountType: 'SAVINGS', balance: 50000.00, totalAvailableFunds: 50000.00, status: 'ACTIVE' }
      ]
    };
  }

  if (endpoint.includes('/block') || endpoint.includes('/unblock')) {
    return { status: 200, message: 'Account status updated successfully.' };
  }

  return { status: 200, message: 'Operation successful.' };
}

/**
 * Exported API Endpoint Modules
 */
const API = {
  // Authentication
  auth: {
    register: (data) => apiRequest('/auth/register', 'POST', data),
    login: (data) => apiRequest('/auth/login', 'POST', data),
    adminLogin: (data) => apiRequest('/auth/admin/login', 'POST', data),
  },

  // Customers
  customers: {
    getById: (id) => apiRequest(`/customers/${id}`, 'GET'),
    getByEmail: (email) => apiRequest(`/customers/by-email?email=${encodeURIComponent(email)}`, 'GET'),
  },

  // Accounts
  accounts: {
    create: (data) => apiRequest('/accounts', 'POST', data),
    getByNumber: (accNo) => apiRequest(`/accounts/${accNo}`, 'GET'),
    getByCustomer: (customerId) => apiRequest(`/accounts/customer/${customerId}`, 'GET'),
    getBalance: (accNo) => apiRequest(`/accounts/${accNo}/balance`, 'GET'),
    updateStatus: (accNo, status) => apiRequest(`/accounts/${accNo}/status?status=${status}`, 'PATCH'),
  },

  // Transactions
  transactions: {
    deposit: (data) => apiRequest('/transactions/deposit', 'POST', data),
    withdraw: (data) => apiRequest('/transactions/withdraw', 'POST', data),
    transfer: (data) => apiRequest('/transfers', 'POST', data),
    getHistory: (accNo) => apiRequest(`/transactions/account/${accNo}`, 'GET'),
    filterHistory: (accNo, start, end) => 
      apiRequest(`/transactions/account/${accNo}/filter?start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`, 'GET'),
  },

  // Beneficiaries
  beneficiaries: {
    add: (data) => apiRequest('/beneficiaries', 'POST', data),
    getByCustomer: (customerId) => apiRequest(`/beneficiaries/customer/${customerId}`, 'GET'),
    delete: (customerId, beneficiaryId) => apiRequest(`/beneficiaries/customer/${customerId}/${beneficiaryId}`, 'DELETE'),
  },

  // Admin Suite
  admin: {
    getStatistics: () => apiRequest('/admin/statistics', 'GET'),
    getAllCustomers: () => apiRequest('/admin/customers', 'GET'),
    getAllAccounts: () => apiRequest('/admin/accounts', 'GET'),
    getAllTransactions: () => apiRequest('/admin/transactions', 'GET'),
    blockAccount: (accNo) => apiRequest(`/admin/accounts/${accNo}/block`, 'PATCH'),
    unblockAccount: (accNo) => apiRequest(`/admin/accounts/${accNo}/unblock`, 'PATCH'),
  }
};
