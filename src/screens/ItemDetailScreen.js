
import React, { useState } from 'react';
import { View, StyleSheet, Image, ScrollView, Alert } from 'react-native';
import { Card, Title, Paragraph, Button, TextInput, IconButton } from 'react-native-paper';
import { GoogleSheetsService } from '../services/GoogleSheetsService';

const ItemDetailScreen = ({ route, navigation }) => {
  const { item } = route.params;
  const [quantity, setQuantity] = useState(parseInt(item.quantity) || 0);
  const [editingQuantity, setEditingQuantity] = useState(false);
  const [tempQuantity, setTempQuantity] = useState(quantity.toString());
  const [loading, setLoading] = useState(false);

  const updateQuantity = async (newQuantity) => {
    setLoading(true);
    try {
      await GoogleSheetsService.updateQuantity(item.sku, newQuantity);
      setQuantity(newQuantity);
      Alert.alert('Success', 'Quantity updated!');
    } catch (error) {
      Alert.alert('Error', 'Failed to update quantity');
    }
    setLoading(false);
  };

  const handleIncrement = () => {
    const newQuantity = quantity + 1;
    updateQuantity(newQuantity);
  };

  const handleDecrement = () => {
    if (quantity > 0) {
      const newQuantity = quantity - 1;
      updateQuantity(newQuantity);
    }
  };

  const handleSaveEdit = () => {
    const newQuantity = parseInt(tempQuantity) || 0;
    updateQuantity(newQuantity);
    setEditingQuantity(false);
  };

  return (
    <ScrollView style={styles.container}>
      <Card style={styles.card}>
        {item.imageurl && (
          <Card.Cover
            source={{ uri: item.imageurl }}
            style={styles.image}
          />
        )}

        <Card.Content>
          <Title style={styles.title}>{item.name}</Title>
          <Paragraph style={styles.description}>{item.description}</Paragraph>

          <View style={styles.infoRow}>
            <Paragraph><strong>SKU:</strong> {item.sku}</Paragraph>
          </View>

          <View style={styles.infoRow}>
            <Paragraph><strong>Supplier:</strong> {item.supplier}</Paragraph>
          </View>

          <View style={styles.quantitySection}>
            <Paragraph style={styles.quantityLabel}>Quantity:</Paragraph>

            {editingQuantity ? (
              <View style={styles.editQuantity}>
                <TextInput
                  value={tempQuantity}
                  onChangeText={setTempQuantity}
                  keyboardType="numeric"
                  style={styles.quantityInput}
                />
                <Button onPress={handleSaveEdit} mode="contained" compact>
                  Save
                </Button>
                <Button onPress={() => setEditingQuantity(false)} compact>
                  Cancel
                </Button>
              </View>
            ) : (
              <View style={styles.quantityControls}>
                <IconButton
                  icon="minus"
                  onPress={handleDecrement}
                  disabled={loading || quantity <= 0}
                />
                <Title
                  style={styles.quantityNumber}
                  onPress={() => {
                    setTempQuantity(quantity.toString());
                    setEditingQuantity(true);
                  }}
                >
                  {quantity}
                </Title>
                <IconButton
                  icon="plus"
                  onPress={handleIncrement}
                  disabled={loading}
                />
              </View>
            )}
          </View>
        </Card.Content>
      </Card>

      <Button
        mode="outlined"
        onPress={() => navigation.goBack()}
        style={styles.backButton}
      >
        Back to Inventory
      </Button>
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
    marginBottom: 15,
  },
  image: {
    height: 200,
  },
  title: {
    fontSize: 24,
    marginBottom: 10,
  },
  description: {
    fontSize: 16,
    marginBottom: 15,
  },
  infoRow: {
    marginBottom: 8,
  },
  quantitySection: {
    marginTop: 20,
    padding: 15,
    backgroundColor: '#e3f2fd',
    borderRadius: 8,
  },
  quantityLabel: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  quantityControls: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  quantityNumber: {
    fontSize: 32,
    marginHorizontal: 20,
    minWidth: 60,
    textAlign: 'center',
  },
  editQuantity: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  quantityInput: {
    width: 80,
  },
  backButton: {
    marginTop: 10,
  },
});

export default ItemDetailScreen;

