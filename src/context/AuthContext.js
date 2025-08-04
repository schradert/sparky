
import React, { createContext, useContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { GoogleSheetsService } from '../services/GoogleSheetsService';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const userData = await AsyncStorage.getItem('user');
      const loginTime = await AsyncStorage.getItem('loginTime');

      if (userData && loginTime) {
        const oneWeek = 7 * 24 * 60 * 60 * 1000;
        if (Date.now() - parseInt(loginTime) < oneWeek) {
          setUser(JSON.parse(userData));
        } else {
          await logout();
        }
      }
    } catch (error) {
      console.error('Auth check error:', error);
    }
    setLoading(false);
  };

  const sendLoginEmail = async (email) => {
    try {
      // Check if email is in approved list
      const isApproved = await GoogleSheetsService.checkApprovedEmail(email);
      if (!isApproved) {
        throw new Error('Email not authorized');
      }

      // In a real app, you'd send an email with a unique token
      // For demo purposes, we'll simulate this
      const token = Math.random().toString(36).substr(2, 9);
      console.log(`Login token for ${email}: ${token}`);
      return token;
    } catch (error) {
      throw error;
    }
  };

  const confirmLogin = async (email, token) => {
    try {
      // In a real app, you'd verify the token
      // For demo, we'll accept any token
      const userData = { email, loginTime: Date.now() };
      await AsyncStorage.setItem('user', JSON.stringify(userData));
      await AsyncStorage.setItem('loginTime', Date.now().toString());
      setUser(userData);
      return true;
    } catch (error) {
      throw error;
    }
  };

  const logout = async () => {
    await AsyncStorage.removeItem('user');
    await AsyncStorage.removeItem('loginTime');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      sendLoginEmail,
      confirmLogin,
      logout
    }}>
      {children}
    </AuthContext.Provider>
  );
};

