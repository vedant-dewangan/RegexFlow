import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Landing from './pages/Landing/Landing';
import Login from './pages/Login/Login';
import Register from './pages/Register/Register';
import Dashboard from './pages/Dashboard/Dashboard';
import AllTransactions from './pages/AllTransactions/AllTransactions';
import MonthlyExpense from './pages/MonthlyExpense/MonthlyExpense';
import MakerDashboard from './pages/MakerDashboard/MakerDashboard';
import CheckerDashboard from './pages/CheckerDashboard/CheckerDashboard';
import AdminDashboard from './pages/AdminDashboard/AdminDashboard';
import AdminUser from './pages/AdminUser/AdminUser';
import AdminBank from './pages/AdminBank/AdminBank';
import AdminRegex from './pages/AdminRegex/AdminRegex';
import TemplateEditor from './pages/TemplateEditor/TemplateEditor';
import NotFound from './pages/NotFound/NotFound';
import { AuthProvider } from './context/AuthContext';
import PublicRoute from './context/PublicRoute';
import PrivateRoute from './context/PrivateRoute';
import RoleBasedRoute from './context/RoleBasedRoute';
import './App.css';

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
        {/* Public routes */}
        <Route path="/" element={<Landing />} />
        <Route 
          path="/login" 
          element={
            <PublicRoute>
              <Login />
            </PublicRoute>
          } 
        />
        <Route 
          path="/register" 
          element={
            <PublicRoute>
              <Register />
            </PublicRoute>
          } 
        />
        
        {/* Private routes - require authentication */}
        {/* CUSTOMER can only access /dashboard */}
        <Route 
          path="/dashboard" 
          element={
            <RoleBasedRoute allowedRoles="CUSTOMER">
              <Dashboard />
            </RoleBasedRoute>
          } 
        />
        <Route 
          path="/dashboard/transactions" 
          element={
            <RoleBasedRoute allowedRoles="CUSTOMER">
              <AllTransactions />
            </RoleBasedRoute>
          }
        />
        <Route 
          path="/dashboard/monthly-expense" 
          element={
            <RoleBasedRoute allowedRoles="CUSTOMER">
              <MonthlyExpense />
            </RoleBasedRoute>
          }
        />
        
        {/* MAKER can only access /maker/dashboard and /maker/template/* */}
        <Route 
          path="/maker/dashboard" 
          element={
            <RoleBasedRoute allowedRoles="MAKER">
              <MakerDashboard />
            </RoleBasedRoute>
          } 
        />
        <Route 
          path="/maker/template/new" 
          element={
            <RoleBasedRoute allowedRoles="MAKER">
              <TemplateEditor />
            </RoleBasedRoute>
          } 
        />
        <Route 
          path="/maker/template/:templateId" 
          element={
            <RoleBasedRoute allowedRoles="MAKER">
              <TemplateEditor />
            </RoleBasedRoute>
          } 
        />
        
        {/* CHECKER can only access /checker/dashboard */}
        <Route 
          path="/checker/dashboard" 
          element={
            <RoleBasedRoute allowedRoles="CHECKER">
              <CheckerDashboard />
            </RoleBasedRoute>
          } 
        />
        
        {/* ADMIN can only access /admin and related admin routes */}
        <Route 
          path="/admin" 
          element={
            <RoleBasedRoute allowedRoles="ADMIN">
              <AdminDashboard />
            </RoleBasedRoute>
          } 
        />
        <Route 
          path="/admin/user" 
          element={
            <RoleBasedRoute allowedRoles="ADMIN">
              <AdminUser />
            </RoleBasedRoute>
          } 
        />
        <Route 
          path="/admin/bank" 
          element={
            <RoleBasedRoute allowedRoles="ADMIN">
              <AdminBank />
            </RoleBasedRoute>
          } 
        />
        <Route 
          path="/admin/regex" 
          element={
            <RoleBasedRoute allowedRoles="ADMIN">
              <AdminRegex />
            </RoleBasedRoute>
          } 
        />
        <Route path="*" element={<NotFound />} />
        </Routes>
        <Toaster position="top-right" />
      </AuthProvider>
    </Router>
  );
}

export default App;
