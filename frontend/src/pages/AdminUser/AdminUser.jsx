import { useState, useEffect } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import axios from 'axios';
import toast from 'react-hot-toast';
import './AdminUser.css';

function AdminUser() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updatingRole, setUpdatingRole] = useState(null);
  const [selectedRoles, setSelectedRoles] = useState({});

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/user', {
        withCredentials: true,
        headers: {
          'Content-Type': 'application/json',
        },
      });
      setUsers(response.data);
      // Initialize selected roles with current roles
      const initialRoles = {};
      response.data.forEach((user) => {
        initialRoles[user.userId] = user.role;
      });
      setSelectedRoles(initialRoles);
    } catch (error) {
      console.error('Error fetching users:', error);
      toast.error(error.response?.data?.message || 'Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  const handleRoleChange = (userId, newRole) => {
    setSelectedRoles((prev) => ({
      ...prev,
      [userId]: newRole,
    }));
  };

  const updateUserRole = async (userId, currentRole) => {
    const newRole = selectedRoles[userId];
    
    // Don't update if role hasn't changed
    if (newRole === currentRole) {
      toast.error('Please select a different role');
      return;
    }

    // Prevent updating ADMIN role
    if (currentRole === 'ADMIN') {
      toast.error('Admin role cannot be updated');
      return;
    }

    try {
      setUpdatingRole(userId);
      const response = await axios.put(
        `http://localhost:8080/user/${userId}`,
        { role: newRole },
        {
          withCredentials: true,
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );
      
      toast.success('User role updated successfully');
      // Update the user in the list
      setUsers((prevUsers) =>
        prevUsers.map((user) =>
          user.userId === userId ? response.data : user
        )
      );
      // Update selected role to match the new role
      setSelectedRoles((prev) => ({
        ...prev,
        [userId]: response.data.role,
      }));
    } catch (error) {
      console.error('Error updating user role:', error);
      toast.error(error.response?.data?.message || 'Failed to update user role');
      // Reset to original role on error
      setSelectedRoles((prev) => ({
        ...prev,
        [userId]: currentRole,
      }));
    } finally {
      setUpdatingRole(null);
    }
  };

  return (
    <div className="admin-user-container">
      <Navbar />
      <div className="admin-user-header">
        <h1>User Management</h1>
        <p>Manage all users and assign roles</p>
      </div>
      <div className="admin-user-content">
        <div className="admin-user-card">
          <h2>All Users</h2>
          {loading ? (
            <p>Loading users...</p>
          ) : users.length === 0 ? (
            <p>No users found</p>
          ) : (
            <div className="users-table-container">
              <table className="users-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Current Role</th>
                    <th>New Role</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <tr key={user.userId}>
                      <td>{user.userId}</td>
                      <td>{user.name}</td>
                      <td>{user.email}</td>
                      <td>
                        <span className={`role-badge role-${user.role.toLowerCase()}`}>
                          {user.role}
                        </span>
                      </td>
                      <td>
                        <select
                          className="role-select"
                          value={selectedRoles[user.userId] || user.role}
                          onChange={(e) => handleRoleChange(user.userId, e.target.value)}
                          disabled={user.role === 'ADMIN' || updatingRole === user.userId}
                        >
                          <option value="MAKER">MAKER</option>
                          <option value="CHECKER">CHECKER</option>
                          <option value="CUSTOMER">CUSTOMER</option>
                          {user.role === 'ADMIN' && <option value="ADMIN">ADMIN</option>}
                        </select>
                      </td>
                      <td>
                        <button
                          className="btn-update-role"
                          onClick={() => updateUserRole(user.userId, user.role)}
                          disabled={
                            user.role === 'ADMIN' ||
                            updatingRole === user.userId ||
                            (selectedRoles[user.userId] || user.role) === user.role
                          }
                        >
                          {updatingRole === user.userId ? 'Updating...' : 'Update Role'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
      <Footer />
    </div>
  );
}

export default AdminUser;
