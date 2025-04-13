import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import AuthPage from './pages/AuthPage';
import WelcomePage from './pages/WelcomePage';
import GymQuestionPage from './pages/GymQuestionPage';
import WorkoutSuccessPage from './pages/WorkoutSuccessPage';
import WorkoutMissedPage from './pages/WorkoutMissedPage';
import CalendarPage from './pages/CalendarPage';

// Import styles directly - adjust paths as needed for your project structure
import '../src/styles/CalendarStyles.css';
import '../src/styles/CalendarPageStyles.css';

// Simple authentication check
const PrivateRoute = ({ children }) => {
    const token = localStorage.getItem('token');

    if (!token) {
        return <Navigate to="/" replace />;
    }

    return children;
};

const App = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<AuthPage />} />
                <Route
                    path="/welcome"
                    element={
                        <PrivateRoute>
                            <WelcomePage />
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/gym-question"
                    element={
                        <PrivateRoute>
                            <GymQuestionPage />
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/workout-success"
                    element={
                        <PrivateRoute>
                            <WorkoutSuccessPage />
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/workout-missed"
                    element={
                        <PrivateRoute>
                            <WorkoutMissedPage />
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/calendar"
                    element={
                        <PrivateRoute>
                            <CalendarPage />
                        </PrivateRoute>
                    }
                />
                {/* Catch all route */}
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </Router>
    );
};

export default App;