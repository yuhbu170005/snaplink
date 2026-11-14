import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProtectedRoute from './components/ProtectedRoute';
const AnalyticsPage = lazy(() => import('./pages/AnalyticsPage'));
const DashboardPage = lazy(() => import('./pages/DashboardPage'));

/** Khai báo route. AuthProvider bao toàn bộ app; ProtectedRoute chặn trang cần đăng nhập. */
function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Suspense fallback={<p role="status" className="p-8">Đang tải trang...</p>}>
          <Routes>
            {/* Trang public: không yêu cầu đăng nhập. */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Trang riêng: đợi xác thực xong rồi mới render. */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <DashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/analytics/:id"
              element={
                <ProtectedRoute>
                  <AnalyticsPage />
                </ProtectedRoute>
              }
            />

            {/* Route không tồn tại quay về trang chủ. */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
