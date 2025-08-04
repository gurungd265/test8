import api from './index';
const paymentsApi = {
    createPayment: async (paymentRequest) => {
        try {
            const response = await api.post('/payments', paymentRequest);
            return response.data;
        } catch (error) {
            console.error("[API] 支払い作成に失敗しました:", error);
            throw error;
        }
    },

    /**
     * 支払いをキャンセルする
     * @param {string} transactionId - キャンセルする取引ID
     * @returns {Promise<object>} 成功時はキャンセル結果データ、失敗時はエラー
     */
    cancelPayment: async (transactionId) => {
        try {
            const response = await api.post(`/payments/cancel/${transactionId}`);
            return response.data;
        } catch (error) {
            console.error(`[API] 取引ID ${transactionId} のキャンセルに失敗しました:`, error);
            throw error;
        }
    },

    /**
     * 支払いを払い戻す
     * @param {string} transactionId - 払い戻しを行う取引ID
     * @param {number} refundAmount - 払い戻し金額
     * @returns {Promise<object>} 成功時は払い戻し結果データ、失敗시는エラー
     */
    refundPayment: async (transactionId, refundAmount) => {
        try {
            // パラメータをリクエストボディに含める
            const response = await api.post(`/payments/refund/${transactionId}`, { refundAmount });
            return response.data;
        } catch (error) {
            console.error(`[API] 取引ID ${transactionId} の払い戻しに失敗しました:`, error);
            throw error;
        }
    },

    /**
     * 特定のステータスの支払いをすべて取得する
     * @param {string} status - 取得したい支払いのステータス（例: 'COMPLETED'）
     * @returns {Promise<Array>} 成功時は支払いリスト、失敗時はエラー
     */
    getPaymentsByStatus: async (status) => {
        try {
            const response = await api.get(`/payments/status/${status}`);
            return response.data;
        } catch (error) {
            console.error(`[API] ステータス ${status} の支払い取得に失敗しました:`, error);
            throw error;
        }
    }
};

export default paymentsApi;
