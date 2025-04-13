// src/services/api.js
import axios from 'axios';

// Create an axios instance with default config
const API = axios.create({
    baseURL: 'http://localhost:8080/api', // Replace with your backend URL
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add a request interceptor to inject auth token into requests
API.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Add a response interceptor to handle common errors
API.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        // Handle session expiration or unauthorized access
        if (error.response && error.response.status === 401) {
            // Clear local storage and redirect to login
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('userId');

            // If we're not already on the auth page, redirect
            if (window.location.pathname !== '/') {
                window.location.href = '/';
            }
        }
        return Promise.reject(error);
    }
);

// Auth services
export const authService = {
    login: (credentials) => API.post('/auth/login', credentials),
    register: (userData) => API.post('/auth/register', userData),
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('userId');
    }
};

// Workout tracking services
export const workoutService = {
    getWorkoutDays: () => API.get('/workouts'),
    addWorkoutDay: (date) => API.post('/workouts', { date }),
    getWeeklySummary: () => API.get('/workouts/summary')
};

export default API;