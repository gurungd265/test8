import api from './index';

const productsApi = {
    getAllProducts: async () => {
        try {
            const response = await api.get('/api/products');
            return response.data;
        } catch (error) {
            console.error('すべての商品読み込み失敗:', error);
            throw error;
        }
    },

    getProductsByFilter: async ({ categoryId, keyword }) => {
        try {
            const params = {};
            if (categoryId) params.categoryId = categoryId;
            if (keyword) params.keyword = keyword;

            const response = await api.get('/api/products', { params });
            return response.data;
        } catch (error) {
            console.error('フィルター付き商品読み込み失敗:', error);
            throw error;
        }
    },

    getProductById: async (productId) => {
        try {
            const response = await api.get(`/api/products/${productId}`);
            return response.data;
        } catch (error) {
            console.error(`商品 ${productId} 読み込み失敗:`, error);
            throw error;
        }
    }
};

export default productsApi;