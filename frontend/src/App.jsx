import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Landing from './pages/Landing/Landing';
import Login from './pages/Login/Login';
import Register from './pages/Register/Register';
import Dashboard from './pages/Dashboard/Dashboard';
import MakerDashboard from './pages/MakerDashboard/MakerDashboard';
import CheckerDashboard from './pages/CheckerDashboard/CheckerDashboard';
import AdminDashboard from './pages/AdminDashboard/AdminDashboard';
import AdminUser from './pages/AdminUser/AdminUser';
import AdminBank from './pages/AdminBank/AdminBank';
import AdminRegex from './pages/AdminRegex/AdminRegex';
import TemplateEditor from './pages/TemplateEditor/TemplateEditor';
import NotFound from './pages/NotFound/NotFound';
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/maker/dashboard" element={<MakerDashboard />} />
        <Route path="/maker/template/new" element={<TemplateEditor />} />
        <Route path="/maker/template/:templateId" element={<TemplateEditor />} />
        <Route path="/checker/dashboard" element={<CheckerDashboard />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/user" element={<AdminUser />} />
        <Route path="/admin/bank" element={<AdminBank />} />
        <Route path="/admin/regex" element={<AdminRegex />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
      <Toaster position="top-right" />
    </Router>
  );
}

export default App;
