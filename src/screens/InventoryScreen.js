
import React, { useState, useEffect } from 'react';
import { View, StyleSheet, FlatList, RefreshControl } from 'react-native';
import { Card, Title, Paragraph, Searchbar, FAB } from 'react-native-paper';
import { GoogleSheetsService } from '../services/GoogleSheetsService';

const InventoryScreen = ({ navigation }) => {
  const [inventory, setInventory] = useState([]);
  const [filteredInventory, setFilteredInventory] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    loadInventory();
  }, []);

  useEffect(() => {
    filterInventory();
  }, [searchQuery, inventory]);

  const loadInventory = async () => {
    setLoading(true);
    try {
      const data = await GoogleSheetsService.getInventoryData();
      setInventory(data);
    } catch (error) {
      console.error('Error loading inventory:', error);
    }
    setLoading(false);
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadInventory();
    setRefreshing(false);
  };

  const filterInventory = () => {
    if (!searchQuery) {
      setFilteredInventory(inventory);
      return;
    }

    const filtered = inventory.filter(item =>
      item.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.description?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.supplier?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.sku?.toLowerCase().includes(searchQuery.toLowerCase())
    );
    setFilteredInventory(filtered);
  };

  const renderItem = ({ item }) => (
    <Card
      style={styles.card}
      onPress={() => navigation.navigate('ItemDetail', { item })}
    >
      <Card.Content>
        <Title>{item.name}</Title>
        <Paragraph numberOfLines={2}>{item.description}</Paragraph>
        <View style={styles.itemDetails}>
          <Paragraph><strong>SKU:</strong> {item.sku}</Paragraph>
          <Paragraph><strong>Quantity:</strong> {item.quantity || 0}</Paragraph>
          <Paragraph><strong>Supplier:</strong> {item.supplier}</Paragraph>
        </View>
      </Card.Content>
    </Card>
  );

  return (
    <View style={styles.container}>
      <Searchbar
        placeholder="Search inventory..."
        onChangeText={setSearchQuery}
        value={searchQuery}
        style={styles.searchbar}
      />

      <FlatList
        data={filteredInventory}
        renderItem={renderItem}
        keyExtractor={(item, index) => item.sku || index.toString()}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        style={styles.list}
      />

      <FAB
        icon="plus"
        style={styles.fab}
        onPress={() => navigation.navigate('AddItem')}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  searchbar: {
    margin: 15,
  },
  list: {
    flex: 1,
    paddingHorizontal: 15,
  },
  card: {
    marginBottom: 10,
  },
  itemDetails: {
    marginTop: 10,
  },
  fab: {
    position: 'absolute',
    margin: 16,
    right: 0,
    bottom: 0,
    backgroundColor: '#6200ee',
  },
});

export default InventoryScreen;

