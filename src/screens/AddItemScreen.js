import React, { useState } from 'react';
import { View, StyleSheet, ScrollView, Alert, Image } from 'react-native';
import { TextInput, Button, Title, Card } from 'react-native-paper';
import { launchImageLibrary, launchCamera } from 'react-native-image-picker';
import { GoogleSheetsService } from '../services/GoogleSheetsService';

const AddItemScreen = ({ route, navigation }) => {
  const { scannedCode } = route.params || {};

  const [formData, setFormData] = useState({
    name: '',
    description: '',
    supplier: '',
    sku: scannedCode || '',
    quantity: '0',
    keywords: '',
  });
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleInputChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const selectImage = () => {
    Alert.alert(
      'Select Image',
      'Choose an option',
      [
        { text: 'Camera', onPress: openCamera },
        { text: 'Gallery', onPress: openGallery },
        { text: 'Cancel', style: 'cancel' },
      ]
    );
  };

  const openCamera = () => {
    launchCamera({ mediaType: 'photo', quality: 0.7 }, handleImageResponse);
  };

  const openGallery = () => {
    launchImageLibrary({ mediaType: 'photo', quality: 0.7 }, handleImageResponse);
  };

  const handleImageResponse = (response) => {
    if (response.assets && response.assets[0]) {
      setImage(response.assets[0]);
    }
  };

  const uploadImage = async (imageAsset) => {
    // In a real app, you'd upload to a cloud service like Firebase Storage, AWS S3, etc.
    // For demo purposes, we'll return a placeholder URL
    return `https://placeholder-image-url.com/${Date.now()}.jpg`;
  };

  const handleSubmit = async () => {
    if (!formData.name || !formData.sku) {
      Alert.alert('Error', 'Name and SKU are required');
      return;
    }

    setLoading(true);
    try {
      let imageUrl = '';
      if (image) {
        imageUrl = await uploadImage(image);
      }

      const itemData = {
        ...formData,
        imageUrl,
        quantity: parseInt(formData.quantity) || 0,
      };

      await GoogleSheetsService.addNewItem(itemData);
      Alert.alert('Success', 'Item added to inventory!', [
        { text: 'OK', onPress: () => navigation.goBack() }
      ]);
    } catch (error) {
      Alert.alert('Error', 'Failed to add item');
      console.error('Add item error:', error);
    }
    setLoading(false);
  };

  return (
    <ScrollView style={styles.container}>
      <Card style={styles.card}>
        <Card.Content>
          <Title style={styles.title}>
            {scannedCode ? 'Register New Item' : 'Add New Item'}
          </Title>

          {scannedCode && (
            <View style={styles.scannedCode}>
              <Paragraph>Scanned Code: {scannedCode}</Paragraph>
            </View>
          )}

          <View style={styles.imageSection}>
            {image ? (
              <Image source={{ uri: image.uri }} style={styles.imagePreview} />
            ) : (
              <View style={styles.imagePlaceholder}>
                <Paragraph>No image selected</Paragraph>
              </View>
            )}
            <Button mode="outlined" onPress={selectImage} style={styles.imageButton}>
              {image ? 'Change Image' : 'Add Image'}
            </Button>
          </View>

          <TextInput
            label="Item Name *"
            value={formData.name}
            onChangeText={(value) => handleInputChange('name', value)}
            mode="outlined"
            style={styles.input}
          />

          <TextInput
            label="Description"
            value={formData.description}
            onChangeText={(value) => handleInputChange('description', value)}
            mode="outlined"
            multiline
            numberOfLines={3}
            style={styles.input}
          />

          <TextInput
            label="Supplier"
            value={formData.supplier}
            onChangeText={(value) => handleInputChange('supplier', value)}
            mode="outlined"
            style={styles.input}
          />

          <TextInput
            label="SKU *"
            value={formData.sku}
            onChangeText={(value) => handleInputChange('sku', value)}
            mode="outlined"
            style={styles.input}
            editable={!scannedCode}
          />

          <TextInput
            label="Initial Quantity"
            value={formData.quantity}
            onChangeText={(value) => handleInputChange('quantity', value)}
            mode="outlined"
            keyboardType="numeric"
            style={styles.input}
          />

          <TextInput
            label="Keywords (season, holiday, purpose, etc.)"
            value={formData.keywords}
            onChangeText={(value) => handleInputChange('keywords', value)}
            mode="outlined"
            placeholder="e.g., birthday, christmas, red, latex"
            style={styles.input}
          />

          <View style={styles.buttonContainer}>
            <Button
              mode="contained"
              onPress={handleSubmit}
              loading={loading}
              style={styles.submitButton}
            >
              Add Item
            </Button>

            <Button
              mode="outlined"
              onPress={() => navigation.goBack()}
              style={styles.cancelButton}
            >
              Cancel
            </Button>
          </View>
        </Card.Content>
      </Card>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 15,
    backgroundColor: '#f5f5f5',
  },
  card: {
    marginBottom: 20,
  },
  title: {
    textAlign: 'center',
    marginBottom: 20,
  },
  scannedCode: {
    backgroundColor: '#e8f5e8',
    padding: 10,
    borderRadius: 5,
    marginBottom: 15,
  },
  imageSection: {
    alignItems: 'center',
    marginBottom: 20,
  },
  imagePreview: {
    width: 200,
    height: 200,
    borderRadius: 10,
    marginBottom: 10,
  },
  imagePlaceholder: {
    width: 200,
    height: 200,
    backgroundColor: '#f0f0f0',
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 10,
  },
  imageButton: {
    marginBottom: 10,
  },
  input: {
    marginBottom: 15,
  },
  buttonContainer: {
    marginTop: 20,
  },
  submitButton: {
    marginBottom: 10,
  },
  cancelButton: {
    marginBottom: 10,
  },
});

export default AddItemScreen;

