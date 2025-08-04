
import axios from 'axios';

const GOOGLE_SHEETS_API_KEY = 'YOUR_API_KEY_HERE';
const INVENTORY_SHEET_ID = 'YOUR_INVENTORY_SHEET_ID';
const APPROVED_EMAILS_SHEET_ID = 'YOUR_APPROVED_EMAILS_SHEET_ID';

export class GoogleSheetsService {
  static async getInventoryData() {
    try {
      const response = await axios.get(
        `https://sheets.googleapis.com/v4/spreadsheets/${INVENTORY_SHEET_ID}/values/Sheet1?key=${GOOGLE_SHEETS_API_KEY}`
      );

      const rows = response.data.values;
      if (!rows || rows.length === 0) return [];

      const headers = rows[0];
      return rows.slice(1).map(row => {
        const item = {};
        headers.forEach((header, index) => {
          item[header.toLowerCase()] = row[index] || '';
        });
        return item;
      });
    } catch (error) {
      console.error('Error fetching inventory:', error);
      return [];
    }
  }

  static async updateQuantity(sku, newQuantity) {
    try {
      // Find the row with the matching SKU
      const data = await this.getInventoryData();
      const rowIndex = data.findIndex(item => item.sku === sku);

      if (rowIndex === -1) throw new Error('Item not found');

      // Update the quantity (assuming quantity is in column F, index 5)
      const range = `Sheet1!F${rowIndex + 2}`;
      await axios.put(
        `https://sheets.googleapis.com/v4/spreadsheets/${INVENTORY_SHEET_ID}/values/${range}?valueInputOption=RAW&key=${GOOGLE_SHEETS_API_KEY}`,
        {
          values: [[newQuantity]]
        }
      );

      return true;
    } catch (error) {
      console.error('Error updating quantity:', error);
      throw error;
    }
  }

  static async addNewItem(itemData) {
    try {
      const range = 'Sheet1!A:G';
      await axios.post(
        `https://sheets.googleapis.com/v4/spreadsheets/${INVENTORY_SHEET_ID}/values/${range}:append?valueInputOption=RAW&key=${GOOGLE_SHEETS_API_KEY}`,
        {
          values: [[
            itemData.name,
            itemData.description,
            itemData.supplier,
            itemData.sku,
            itemData.quantity,
            itemData.imageUrl,
            itemData.keywords
          ]]
        }
      );
      return true;
    } catch (error) {
      console.error('Error adding item:', error);
      throw error;
    }
  }

  static async checkApprovedEmail(email) {
    try {
      const response = await axios.get(
        `https://sheets.googleapis.com/v4/spreadsheets/${APPROVED_EMAILS_SHEET_ID}/values/Sheet1?key=${GOOGLE_SHEETS_API_KEY}`
      );

      const emails = response.data.values?.flat() || [];
      return emails.includes(email);
    } catch (error) {
      console.error('Error checking approved email:', error);
      return false;
    }
  }

  static async searchItems(query) {
    try {
      const data = await this.getInventoryData();
      const lowerQuery = query.toLowerCase();

      return data.filter(item =>
        item.name?.toLowerCase().includes(lowerQuery) ||
        item.description?.toLowerCase().includes(lowerQuery) ||
        item.supplier?.toLowerCase().includes(lowerQuery) ||
        item.keywords?.toLowerCase().includes(lowerQuery) ||
        item.sku?.toLowerCase().includes(lowerQuery)
      );
    } catch (error) {
      console.error('Error searching items:', error);
      return [];
    }
  }
}

