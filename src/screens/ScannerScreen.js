
import React, { useState, useEffect } from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { Camera, useCameraDevices } from 'react-native-vision-camera';
import { useScanBarcodes, BarcodeFormat } from 'vision-camera-code-scanner';
import { Button } from 'react-native-paper';
import { GoogleSheetsService } from '../services/GoogleSheetsService';

const ScannerScreen = ({ navigation }) => {
  const [hasPermission, setHasPermission] = useState(false);
  const [isActive, setIsActive] = useState(true);
  const devices = useCameraDevices();
  const device = devices.back;

  const [frameProcessor, barcodes] = useScanBarcodes([
    BarcodeFormat.QR_CODE,
    BarcodeFormat.EAN_13,
    BarcodeFormat.CODE_128,
  ], {
    checkInverted: true,
  });

  useEffect(() => {
    checkCameraPermission();
  }, []);

  useEffect(() => {
    if (barcodes.length > 0 && isActive) {
      handleBarcodeScanned(barcodes[0]);
    }
  }, [barcodes, isActive]);

  const checkCameraPermission = async () => {
    const status = await Camera.requestCameraPermission();
    setHasPermission(status === 'authorized');
  };

  const handleBarcodeScanned = async (barcode) => {
    setIsActive(false);

    try {
      const inventory = await GoogleSheetsService.getInventoryData();
      const item = inventory.find(item => item.sku === barcode.displayValue);

      if (item) {
        navigation.navigate('ItemDetail', { item });
      } else {
        navigation.navigate('AddItem', { scannedCode: barcode.displayValue });
      }
    } catch (error) {
      console.error('Error processing scanned code:', error);
    }

    setTimeout(() => setIsActive(true), 2000);
  };

  if (!hasPermission) {
    return (
      <View style={styles.container}>
        <Text>Camera permission is required</Text>
        <Button onPress={checkCameraPermission}>Grant Permission</Button>
      </View>
    );
  }

  if (!device) {
    return (
      <View style={styles.container}>
        <Text>No camera device found</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Camera
        style={StyleSheet.absoluteFill}
        device={device}
        isActive={isActive}
        frameProcessor={frameProcessor}
        frameProcessorFps={5}
      />
      <View style={styles.overlay}>
        <View style={styles.scanArea} />
        <Text style={styles.instruction}>
          Point camera at barcode
        </Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  overlay: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  scanArea: {
    width: 250,
    height: 250,
    borderWidth: 2,
    borderColor: 'white',
    backgroundColor: 'transparent',
  },
  instruction: {
    color: 'white',
    fontSize: 16,
    marginTop: 20,
    textAlign: 'center',
  },
});

export default ScannerScreen;

