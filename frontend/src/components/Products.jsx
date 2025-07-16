import Product from './Product';

export default function Products() {
    return (
        <>
                  {/* Products */}
      <main className="container mx-auto p-4 mb-20">
        <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
            <Product />
        </section>
      </main>
        </>
    )
}