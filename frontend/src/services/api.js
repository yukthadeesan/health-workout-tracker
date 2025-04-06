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

// Auth services
export const authService = {
    login: (credentials) => API.post('/auth/login', credentials),
    register: (userData) => API.post('/auth/register', userData),
    logout: () => {
        localStorage.removeItem('token');
    }
};

// Workout tracking services
export const workoutService = {
    getWorkoutDays: () => API.get('/workouts'),
    addWorkoutDay: (date) => API.post('/workouts', { date }),
    getWeeklySummary: () => API.get('/workouts/summary')
};

export default API;