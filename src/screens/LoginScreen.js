
import React, { useState } from 'react';
import { View, StyleSheet, Alert } from 'react-native';
import { TextInput, Button, Title, Card } from 'react-native-paper';
import { useAuth } from '../context/AuthContext';

const LoginScreen = () => {
  const [email, setEmail] = useState('');
  const [token, setToken] = useState('');
  const [step, setStep] = useState('email'); // 'email' or 'token'
  const [loading, setLoading] = useState(false);
  const { sendLoginEmail, confirmLogin } = useAuth();

  const handleSendEmail = async () => {
    if (!email) return;

    setLoading(true);
    try {
      await sendLoginEmail(email);
      Alert.alert('Success', 'Login link sent to your email!');
      setStep('token');
    } catch (error) {
      Alert.alert('Error', error.message || 'Failed to send login email');
    }
    setLoading(false);
  };

  const handleConfirmLogin = async () => {
    if (!token) return;

    setLoading(true);
    try {
      await confirmLogin(email, token);
    } catch (error) {
      Alert.alert('Error', 'Invalid token');
    }
    setLoading(false);
  };

  return (
    <View style={styles.container}>
      <Card style={styles.card}>
        <Card.Content>
          <Title style={styles.title}>Balloon Inventory</Title>

          {step === 'email' ? (
            <>
              <TextInput
                label="Email"
                value={email}
                onChangeText={setEmail}
                mode="outlined"
                keyboardType="email-address"
                style={styles.input}
              />
              <Button
                mode="contained"
                onPress={handleSendEmail}
                loading={loading}
                style={styles.button}
              >
                Send Login Link
              </Button>
            </>
          ) : (
            <>
              <TextInput
                label="Enter token from email"
                value={token}
                onChangeText={setToken}
                mode="outlined"
                style={styles.input}
              />
              <Button
                mode="contained"
                onPress={handleConfirmLogin}
                loading={loading}
                style={styles.button}
              >
                Login
              </Button>
              <Button
                mode="text"
                onPress={() => setStep('email')}
                style={styles.backButton}
              >
                Back to Email
              </Button>
            </>
          )}
        </Card.Content>
      </Card>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  card: {
    padding: 20,
  },
  title: {
    textAlign: 'center',
    marginBottom: 30,
  },
  input: {
    marginBottom: 15,
  },
  button: {
    marginTop: 10,
  },
  backButton: {
    marginTop: 10,
  },
});

export default LoginScreen;

