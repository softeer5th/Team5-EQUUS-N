import React from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Route, Routes, useLocation } from 'react-router';
import 'react-datepicker/dist/react-datepicker.css';
import './index.css';
import './customDatePicker.css';
import Layout from './Layout';
import FeedbackRequest from './pages/feedback/FeedbackRequest';
import Splash from './pages/auth/Splash';
import SignIn from './pages/auth/SignIn';
import SignUp from './pages/auth/SignUp';
import TeamSpaceMake from './pages/teamspace/TeamSpaceMake';
import TeamSpaceMakeSuccess from './pages/teamspace/TeamSpaceMakeSuccess';
import Calendar from './pages/calendar/Calendar';
import MainPage from './pages/main/MainPage';
import TeamSpaceList from './pages/teamspace/TeamSpaceList';
import NotificationPage from './pages/main/NotificationPage';
import FeedbackHistory from './pages/feedback/FeedbackHistory';
import TeamSpaceManage from './pages/teamspace/TeamSpaceManage';
import TeamSpaceEdit from './pages/teamspace/TeamSpaceEdit';
import FeedbackComplete from './pages/feedback/FeedbackComplete';
import FeedbackSelf from './pages/feedback/FeedbackSelf';
import CombinedProvider from './CombinedProvider';
import FeedbackSendLayout from './pages/feedback/FeedbackSendLayout';
import FeedbackSendStep from './pages/feedback/FeedbackSendStep';
import FeedbackSend from './pages/feedback/FeedbackSend';
import FeedbackFavorite from './pages/feedback/FeedbackFavorite';
import MyPageHome from './pages/mypage/MyPageHome';
import ProtectedRoute from './ProtectedRoute';
import SplashForOAuth from './pages/auth/SplashForOAuth';
import FeedbackSendFreq from './pages/feedback/FeedbackSendFreq';
import ProfileEdit from './pages/mypage/ProfileEdit';
import Report from './pages/mypage/Report';
import PasswordReset from './pages/auth/PasswordReset';
import { motion, AnimatePresence } from 'motion/react';

const queryClient = new QueryClient();

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <CombinedProvider>
          <AnimatedRoutes />
        </CombinedProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

function AnimatedRoutes() {
  const location = useLocation();

  return (
    <AnimatePresence mode='wait'>
      <Routes key={location.pathname}>
        <Route element={<Layout />}>
          <Route path='/:teamCode?' element={<Splash />} />
          <Route path='/login/google' element={<SplashForOAuth />} />
          <Route path='signin' element={<SignIn />} />
          <Route path='signup' element={<SignUp />} />
          <Route path='password/reset' element={<PasswordReset />} />
          {/* 이 아래는 로그인 해야 이용 가능 */}
          <Route element={<ProtectedRoute />}>
            <Route path='feedback'>
              <Route path='request' element={<FeedbackRequest />} />
              <Route path='send' element={<FeedbackSendLayout />}>
                <Route index element={<FeedbackSend />} />
                <Route path='frequent' element={<FeedbackSendFreq />} />
                <Route path=':step' element={<FeedbackSendStep />} />
              </Route>
              <Route path='self' element={<FeedbackSelf />} />
              <Route path='complete' element={<FeedbackComplete />} />
              <Route path='favorite' element={<FeedbackFavorite />} />
              <Route path='received' element={<FeedbackHistory />} />
              <Route path='sent' element={<FeedbackHistory />} />
            </Route>
            <Route path='teamspace'>
              <Route path='make'>
                <Route index element={<TeamSpaceMake />} />
                <Route
                  path='first'
                  element={<TeamSpaceMake isFirst={true} />}
                />
                <Route path='success' element={<TeamSpaceMakeSuccess />} />
              </Route>
              <Route path='list' element={<TeamSpaceList />} />
              <Route path='manage/:teamId'>
                <Route index element={<TeamSpaceManage />} />
                <Route path='edit' element={<TeamSpaceEdit />} />
              </Route>
            </Route>
            <Route path='calendar' element={<Calendar />} />
            <Route path='main'>
              <Route index element={<MainPage />} />
              <Route
                path='notification'
                element={
                  <PageWrapper>
                    <NotificationPage />
                  </PageWrapper>
                }
              />
            </Route>
            <Route path='mypage'>
              <Route index element={<MyPageHome />} />
              <Route path='self' element={<FeedbackHistory />} />
              <Route path='report' element={<Report />} />
              <Route path='edit' element={<ProfileEdit />} />
            </Route>
          </Route>
        </Route>
      </Routes>
    </AnimatePresence>
  );
}

function PageWrapper({ children }) {
  return (
    <motion.div
      initial={{ opacity: 0, x: 50 }} // 처음 로드될 때의 상태
      animate={{ opacity: 1, x: 0 }} // 활성화될 때
      exit={{ opacity: 0, x: -50 }} // 제거될 때
      transition={{ duration: 0.3 }}
    >
      {children}
    </motion.div>
  );
}
