import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/AuthPage.css'; // Import the CSS file

const AuthPage = () => {
    const [isLogin, setIsLogin] = useState(true);
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        // Clear any existing tokens on component mount
        localStorage.removeItem('token');
        console.log("AuthPage mounted - cleared any existing auth tokens");
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        console.log("Form submitted", { isLogin, username, password });

        // Validate inputs
        if (!username || !password) {
            setError('Please fill in all fields');
            return;
        }

        if (!isLogin && password !== confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        try {
            console.log(`Attempting to ${isLogin ? 'login' : 'register'} with:`, { username });

            const apiUrl = `http://localhost:8080/api/auth/${isLogin ? 'login' : 'register'}`;
            const userData = { username, password };

            console.log("Sending request to:", apiUrl, userData);
            const response = await axios.post(apiUrl, userData);
            console.log("Response received:", response);
            console.log("Response data:", response.data);

            if (response.data.success) {
                // Store user info in localStorage
                localStorage.setItem('username', response.data.username || username);
                localStorage.setItem('userId', response.data.userId);
                // Add a simple token for PrivateRoute authentication
                localStorage.setItem('token', 'user-authenticated');

                console.log("Stored in localStorage:", {
                    username: localStorage.getItem('username'),
                    userId: localStorage.getItem('userId'),
                    token: localStorage.getItem('token')
                });

                // Navigate to welcome page
                navigate('/welcome');
            } else {
                console.error("Authentication failed:", response.data.message);
                setError(response.data.message || 'Authentication failed');
            }
        } catch (error) {
            console.error("Authentication error:", error);

            // Log more details about the error
            if (error.response) {
                console.error("Response status:", error.response.status);
                console.error("Response data:", error.response.data);
            }

            // Handle API error responses
            if (error.response && error.response.data) {
                setError(error.response.data.message || 'Authentication failed');
            } else {
                setError('Unable to connect to the server. Please try again later.');
            }
        }
    };

    return (
        <div className="auth-container flex items-center justify-center min-h-screen">
            <div className="auth-card w-full max-w-md p-8 space-y-8">
                <div className="text-center">
                    <h1 className="auth-title">Workout Tracker</h1>
                    <h2 className="auth-subtitle">
                        {isLogin ? 'Sign In' : 'Create Account'}
                    </h2>
                </div>

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="mt-8 space-y-6">
                    <div className="form-group">
                        <label htmlFor="username" className="form-label">
                            Username
                        </label>
                        <input
                            id="username"
                            name="username"
                            type="text"
                            required
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            className="form-input"
                            placeholder="Enter your username"
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password" className="form-label">
                            Password
                        </label>
                        <input
                            id="password"
                            name="password"
                            type="password"
                            required
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="form-input"
                            placeholder="Enter your password"
                        />
                    </div>

                    {!isLogin && (
                        <div className="form-group">
                            <label htmlFor="confirmPassword" className="form-label">
                                Confirm Password
                            </label>
                            <input
                                id="confirmPassword"
                                name="confirmPassword"
                                type="password"
                                required
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                className="form-input"
                                placeholder="Confirm your password"
                            />
                        </div>
                    )}

                    <div>
                        <button
                            type="submit"
                            className="btn-primary"
                        >
                            {isLogin ? 'Sign In' : 'Create Account'}
                        </button>
                    </div>
                </form>

                <div className="text-center mt-4">
                    <button
                        type="button"
                        onClick={() => {
                            setIsLogin(!isLogin);
                            setError('');
                        }}
                        className="btn-link"
                    >
                        {isLogin ? 'Need an account? Sign up' : 'Already have an account? Sign in'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AuthPage;