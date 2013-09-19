package net.q1cc.cfs.obacht;

/**
 *
 */
class Pair<T0, T1> {
	public final T0 a;
	public final T1 b;

	public Pair(T0 a, T1 b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Pair<T0, T1> other = (Pair<T0, T1>) obj;
		if (this.a != other.a && (this.a == null || !this.a.equals(other.a))) {
			return false;
		}
		if (this.b != other.b && (this.b == null || !this.b.equals(other.b))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 73 * hash + (this.a != null ? this.a.hashCode() : 0);
		hash = 73 * hash + (this.b != null ? this.b.hashCode() : 0);
		return hash;
	}

}
