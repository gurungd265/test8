import { useState,useEffect,useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import cartApi from '../api/cart';
import {CartContext} from '../contexts/CartContext';


export default function CartPage() {
  const [cart, setCart] = useState(null);
  const [loading,setLoading] = useState(true);
  const [error,setError] = useState(null);

  const { cartItemCount, setCartItemCount, fetchCartCount } = useContext(CartContext);

  const navigate = useNavigate();

    useEffect(() => {
      const fetchCart = async () => {
          try {
              setLoading(true);
              setError(null);

              const cartData = await cartApi.getCartItems();
              setCart(cartData);

          } catch (err) {
              console.error('カートデータを読み込むことができませんでした。', err);
              setError('カートデータを読み込むことができませんでした。');
          } finally {
              setLoading(false);
          }
      };
    fetchCart();
  }, []);

  if (loading) return <div className="p-6 text-center text-gray-700">カート情報を読み込み中...</div>;
  if (error) return <div className="p-6 text-center text-red-600">{error}</div>;
  if (!cart || !cart.items) return <div className="p-6 text-center text-gray-500">カート情報がありません。</div>;

  const handleRemove = (id) => {
      const previousCartItemCount = cartItemCount;
      setCartItemCount(prevCount => Math.max(0, prevCount - 1));
      const removeCartItem = async (cartItemId) => {
          try {
              await cartApi.removeCartItem(cartItemId);
              fetchCartCount();
          } catch (err) {
              console.error('カートアイテム削除失敗:', err);
              setCartItemCount(previousCartItemCount);
              alert('カートアイテムを削除できませんでした。');
              }
          };
        removeCartItem(id);
      };


  const handleQuantityChange = (cartItemId, currentQuantity, delta) => {
      const newQuantity = Math.max(1, currentQuantity + delta);
      const updateQuantity = async () => {
          try {
              const updatedItem = await cartApi.updateCartItemQuantity(cartItemId, newQuantity);
              setCart(prevCart => ({
                  ...prevCart,
                  items: prevCart.items.map(item =>
                      item.id === cartItemId ? updatedItem : item // update item
                  )
              }));
          } catch (err) {
              console.error('数量変更失敗:', err);
              alert('数量を変更できませんでした。');
          }
      };
      updateQuantity();
  };


  const total = cart.items.reduce((sum, item) => sum + item.priceAtAddition * item.quantity, 0);

  // 체크아웃 버튼 클릭 시 결제 페이지로 이동하며 cart 데이터를 전달
    const handleCheckout = () => {
        const subtotal = cart.items.reduce(
            (sum, item) => sum + item.priceAtAddition * item.quantity, 0
        );
        navigate('/checkout', {
            state: {
                cartItems: cart.items,
                subtotal,
            },
        });
    };

  return (
      <div className="max-w-6xl mx-auto p-6">
          <h1 className="text-2xl font-bold mb-4">ショッピングカート</h1>

          {cart.items.length === 0 ? (
              <div className="text-gray-600 text-center py-10">
                  カートは空です。
              </div>
          ) : (
              <>
              {/*  Product List */}
              <div className="space-y-4">
                  {cart.items.map((item) => (
                      <div
                        key={item.id}
                        className="flex items-center justify-between bg-white rounded shadow p-4"
                      >
                        <div className="flex items-center gap-4">
                            <Link to={`/product/${item.productId}`}>
                                <img
                                    src={item.productImageUrl }　
                                    alt={item.productName}
                                    className="w-20 h-20 object-cover rounded"
                                />
                            </Link>
                            <div>
                                <Link to={`/product/${item.productId}`}>
                                    <h2 className="font-semibold">{item.productName}</h2>
                                    <p className="text-gray-500">
                                        {item.priceAtAddition.toLocaleString()}円
                                    </p>
                                </Link>
                                {/*Quantity Controls*/}
                                <div className="mt-2 flex items-center gap-2">
                                    <button
                                        onClick={() => handleQuantityChange(item.id,item.quantity, -1)}
                                        className="h-10 w-10 px-2 py-1 border rounded"
                                    >
                                        -
                                    </button>
                                    <span>{item.quantity}</span>
                                    <button
                                        onClick={() => handleQuantityChange(item.id,item.quantity, 1)}
                                        className="h-10 w-10 px-2 py-1 border rounded"
                                    >
                                        ＋
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div className="text-right">
                            <p className="font-bold">
                                {(item.priceAtAddition * item.quantity).toLocaleString()}円
                            </p>
                            <button
                                onClick={() => handleRemove(item.id)}
                                className="text-red-500 text-sm hover:underline mt-2"
                            >
                                削除
                            </button>
                        </div>
                      </div>
                  ))}
              </div>

              {/*Summary*/}
              <div className="mt-8 bg-white rounded shadow p-4 text-right">
                  <h3 className="text-lg font-semibold mb-2">
                      Total: {total.toLocaleString()}円
                  </h3>
                  <button
                      className="bg-purple-600 text-white px-6 py-2 rounded hover:bg-purple-700"
                      onClick={handleCheckout}  // 여기에 onClick 넣음
                  >
                      チェックアウトに進む
                  </button>
              </div>
            </>
          )}
      </div>
  );
}