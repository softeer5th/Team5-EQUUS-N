import React from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Route, Routes } from 'react-router';
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
import FeedbackReceived from './pages/feedback/FeedbackReceived';
import FeedbackSent from './pages/feedback/FeedbackSent';
import TeamSpaceManage from './pages/teamspace/TeamSpaceManage';
import TeamSpaceEdit from './pages/teamspace/TeamSpaceEdit';
import FeedbackComplete from './pages/feedback/FeedbackComplete';
import FeedbackSelf from './pages/feedback/FeedbackSelf';
import SelfFeedback from './pages/mypage/SelfFeedback';
import CombinedProvider from './CombinedProvider';
import { TeamProvider } from './TeamContext';
import FeedbackSendLayout from './pages/feedback/FeedbackSendLayout';
import FeedbackSendStep from './pages/feedback/FeedbackSendStep';
import FeedbackSend from './pages/feedback/FeedbackSend';
import FeedbackFavorite from './pages/feedback/FeedbackFavorite';
import MyPageHome from './pages/mypage/MyPageHome';
import ProtectedRoute from './ProtectedRoute';

const queryClient = new QueryClient();

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <CombinedProvider>
          <Routes>
            <Route element={<Layout />}>
              <Route path='/:teamCode?' element={<Splash />} />
              <Route path='signin' element={<SignIn />} />
              <Route path='signup' element={<SignUp />} />
              {/* 이 아래는 로그인 해야 이용 가능 */}
              <Route element={<ProtectedRoute />}>
                <Route path='feedback'>
                  <Route path='request' element={<FeedbackRequest />} />
                  <Route path='send' element={<FeedbackSendLayout />}>
                    <Route index element={<FeedbackSend />} />
                    <Route path=':step' element={<FeedbackSendStep />} />
                  </Route>
                  <Route path='self' element={<FeedbackSelf />} />
                  <Route path='complete' element={<FeedbackComplete />} />
                  <Route path='favorite' element={<FeedbackFavorite />} />
                  <Route path='received' element={<FeedbackReceived />} />
                  <Route path='sent' element={<FeedbackSent />} />
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
                  <Route path='notification' element={<NotificationPage />} />
                </Route>
                <Route path='mypage'>
                  <Route index element={<MyPageHome />} />
                  <Route path='self' element={<SelfFeedback />} />
                  <Route path='report' element={<div></div>} />
                  <Route path='edit' element={<div></div>} />
                </Route>
              </Route>
            </Route>
          </Routes>
        </CombinedProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
