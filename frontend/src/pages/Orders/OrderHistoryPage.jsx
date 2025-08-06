import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { ChevronDown, ChevronUp } from "lucide-react";
import ordersData from "../../data/orders.json";

export default function OrderHistoryPage() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [expandedOrders, setExpandedOrders] = useState({});

    useEffect(() => {
        const loadOrders = () => {
            try {
                setOrders(ordersData);

                const initialExpanded = {};
                ordersData.forEach(order => {
                    initialExpanded[order.id] = false;
                });
                setExpandedOrders(initialExpanded);
            } catch (err) {
                console.log("注文の読み込み中にエラーが発生しました：", err);
                setError("注文の読み込み中にエラーが発生しました。");
            } finally {
                setLoading(false);
            }
        };

        loadOrders();
    }, []);

    const toggleOrder = (orderId) => {
        setExpandedOrders(prev => ({
            ...prev,
            [orderId]: !prev[orderId]
        }));
    };

    if (loading) {
        return <div className="p-6 text-center">読み込み中...</div>;
    }

    if (error) {
        return <div className="p-6 text-center text-red-600">{error}</div>;
    }

    if (orders.length === 0) {
        return (
            <div className="p-6 text-center">
                <h2 className="text-2xl font-bold mb-6">注文履歴</h2>
                <p>まだ注文はありません</p>
                <Link to="/" className="text-purple-600 hover:underline mt-4 inline-block">
                    ホームページに移動
                </Link>
            </div>
        );
    }

    return (
        <div className="max-w-7xl mx-auto p-4 md:p-6">
            <h2 className="text-2xl font-bold mb-6">注文履歴</h2>

            <div className="space-y-4">
                {orders.map(order => (
                    <div key={order.id} className="border rounded-lg shadow p-4">
                        {/* General information of the order */}
                        <div className="flex justify-between items-start flex-wrap gap-4">
                            <div>
                                <h3 className="font-semibold">注文ID：＃{order.id}</h3>
                                <p className={`text-sm ${
                                    order.status === "お届け済み" ? "text-green-600" :
                                    order.status === "お届け中" ? "text-yellow-600" :
                                    "text-red-600"
                                }`}>
                                    注文状況：{order.status}
                                </p>
                                <p className="text-sm text-gray-600">
                                    注文した日付：{new Date(order.orderDate).toLocaleDateString()}
                                </p>
                            </div>

                            <div className="text-right">
                                <p className="text-sm text-gray-600">
                                    配送日：{
                                        order.deliveryDate ? 
                                        new Date(order.deliveryDate).toLocaleDateString() :
                                        "未指定"
                                    }
                                </p>
                                <p className="text-sm text-gray-600">
                                    お届け先住所：{order.deliveryAddress || "未指定"}
                                </p>
                                <p className="font-bold text-lg">
                                    合計金額：{order.totalPrice.toLocaleString()}円
                                </p>
                            </div>
                        </div>
                        <div className="mt-4 flex items-center">
                            <div className="text-gray-600">
                                {order.items.length} 商品
                            </div>
                            <button 
                                onClick={() => toggleOrder(order.id)}
                                className="flex items-center gap-1 text-purple-600 ml-auto"
                            >
                                商品詳細
                                {expandedOrders[order.id] ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                            </button>
                        </div>
                        

                        {/* Product list (toggle section) */}
                        {expandedOrders[order.id] && (
                            <div className="mt-4 border-t pt-4">
                                {order.items.map(item => (
                                    <div key={item.id} className="flex gap-4 py-3 border-b last:border-b-0">
                                        <div className="w-20 h-20 flex-shrink-0">
                                            <img 
                                                src={item.imageUrl || "https://via.placeholder.com/80"} 
                                                alt={item.name} 
                                                className="w-full h-full object-cover rounded"
                                            />
                                        </div>

                                        <div className="flex-grow">
                                            <Link
                                                to={`/product/${item.productId}`}
                                                className="font-medium hover:text-purple-600 hover:underline"
                                            >
                                                {item.name}
                                            </Link>
                                            <div className="text-sm text-gray-600">
                                                <p>注文数量：{item.quantity}</p>
                                                <p>金額：{item.price.toLocaleString()}円</p>
                                            </div>
                                            {/* {item.discountPrice && (
                                                <p className="text-xs text-purple-600">
                                                    割引：{item.discountPrice.toLocaleString()}円
                                                </p>
                                            )} */}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
}