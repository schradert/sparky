import React, { useState } from 'react';
import { View, StyleSheet, FlatList, Image } from 'react-native';
import {
  Searchbar,
  Card,
  Title,
  Paragraph,
  Chip,
  Button
} from 'react-native-paper';
import { launchImageLibrary } from 'react-native-image-picker';
import { GoogleSheetsService } from '../services/GoogleSheetsService';

const SearchScreen = ({ navigation }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchImage, setSearchImage] = useState(null);
  const [selectedFilters, setSelectedFilters] = useState([]);

  const commonFilters = [
    'Birthday', 'Wedding', 'Christmas', 'Halloween', 'Valentine',
    'Red', 'Blue', 'Gold', 'Silver', 'Pink',
    'Latex', 'Foil', 'Mylar', 'Helium', 'Air-filled'
  ];

  const handleSearch = async () => {
    if (!searchQuery.trim() && selectedFilters.length === 0) return;

    setLoading(true);
    try {
      let query = searchQuery;
      if (selectedFilters.length > 0) {
        query += ' ' + selectedFilters.join(' ');
      }

      const results = await GoogleSheetsService.searchItems(query);
      setSearchResults(results);
    } catch (error) {
      console.error('Search error:', error);
    }
    setLoading(false);
  };

  const handleImageSearch = () => {
    launchImageLibrary(
      {
        mediaType: 'photo',
        quality: 0.7
      },
      (response) => {
        if (response.assets && response.assets[0]) {
          setSearchImage(response.assets[0]);
          // In a real app, you'd use image recognition API here
          // For demo, we'll just show the selected image
        }
      }
    );
  };

  const toggleFilter = (filter) => {
    setSelectedFilters(prev =>
      prev.includes(filter)
        ? prev.filter(f => f !== filter)
        : [...prev, filter]
    );
  };

  const clearSearch = () => {
    setSearchQuery('');
    setSearchResults([]);
    setSearchImage(null);
    setSelectedFilters([]);
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
          {item.keywords && (
            <Paragraph><strong>Keywords:</strong> {item.keywords}</Paragraph>
          )}
        </View>
      </Card.Content>
    </Card>
  );

  return (
    <View style={styles.container}>
      <View style={styles.searchHeader}>
        <Searchbar
          placeholder="Search balloons..."
          onChangeText={setSearchQuery}
          value={searchQuery}
          onSubmitEditing={handleSearch}
          style={styles.searchbar}
        />

        <View style={styles.searchButtons}>
          <Button
            mode="contained"
            onPress={handleSearch}
            loading={loading}
            style={styles.searchButton}
            compact
          >
            Search
          </Button>

          <Button
            mode="outlined"
            onPress={handleImageSearch}
            style={styles.imageSearchButton}
            compact
          >
            📷 Image
          </Button>

          <Button
            mode="text"
            onPress={clearSearch}
            compact
          >
            Clear
          </Button>
        </View>

        {searchImage && (
          <View style={styles.selectedImage}>
            <Image source={{ uri: searchImage.uri }} style={styles.imagePreview} />
            <Paragraph>Visual search (demo)</Paragraph>
          </View>
        )}

        <View style={styles.filtersSection}>
          <Paragraph style={styles.filtersLabel}>Quick Filters:</Paragraph>
          <View style={styles.filtersContainer}>
            {commonFilters.map(filter => (
              <Chip
                key={filter}
                selected={selectedFilters.includes(filter)}
                onPress={() => toggleFilter(filter)}
                style={styles.filterChip}
              >
                {filter}
              </Chip>
            ))}
          </View>
        </View>
      </View>

      <FlatList
        data={searchResults}
        renderItem={renderItem}
        keyExtractor={(item, index) => item.sku || index.toString()}
        style={styles.results}
        ListEmptyComponent={() => (
          searchQuery || selectedFilters.length > 0 ? (
            <View style={styles.emptyState}>
              <Paragraph>No items found</Paragraph>
            </View>
          ) : null
        )}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  searchHeader: {
    padding: 15,
    backgroundColor: 'white',
    elevation: 2,
  },
  searchbar: {
    marginBottom: 10,
  },
  searchButtons: {
    flexDirection: 'row',
    gap: 10,
    marginBottom: 15,
  },
  searchButton: {
    flex: 1,
  },
  imageSearchButton: {
    flex: 1,
  },
  selectedImage: {
    alignItems: 'center',
    marginBottom: 15,
  },
  imagePreview: {
    width: 100,
    height: 100,
    borderRadius: 10,
    marginBottom: 5,
  },
  filtersSection: {
    marginTop: 10,
  },
  filtersLabel: {
    fontWeight: 'bold',
    marginBottom: 10,
  },
  filtersContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  filterChip: {
    marginBottom: 5,
  },
  results: {
    flex: 1,
    paddingHorizontal: 15,
    paddingTop: 10,
  },
  card: {
    marginBottom: 10,
  },
  itemDetails: {
    marginTop: 10,
  },
  emptyState: {
    alignItems: 'center',
    marginTop: 50,
  },
});

export default SearchScreen;
