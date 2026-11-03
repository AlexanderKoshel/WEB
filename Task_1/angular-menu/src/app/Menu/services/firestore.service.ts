import { Injectable, signal } from '@angular/core';
import { FirebaseOptions, initializeApp } from 'firebase/app';
import { getAuth, onAuthStateChanged, signInWithEmailAndPassword, signOut } from 'firebase/auth';
import { addDoc, collection, deleteDoc, doc, getFirestore, onSnapshot, updateDoc } from 'firebase/firestore';
import { Menu } from '../menu';

@Injectable({ providedIn: 'root' })
export class FirestoreService {
  private readonly connection = this.connect();
  private readonly signedIn = signal(false);
  readonly authenticated = this.signedIn.asReadonly();
  readonly error = signal('');

  private async connect() {
    const response = await fetch('firebase-config.json');
    if (!response.ok) throw new Error('Firebase configuration is missing');
    const config: FirebaseOptions = await response.json();
    const app = initializeApp(config);
    const auth = getAuth(app);
    onAuthStateChanged(auth, user => this.signedIn.set(Boolean(user)));
    return { auth, dishes: collection(getFirestore(app), 'list-menu') };
  }

  async watch(update: (dishes: Menu[]) => void) {
    try {
      const { dishes } = await this.connection;
      onSnapshot(dishes, snapshot => {
        update(snapshot.docs.map(item => ({
          id: item.id,
          description: item.get('description'),
          price: item.get('price'),
        })));
      }, () => this.error.set('Не удалось загрузить меню из Firestore.'));
    } catch {
      this.error.set('Не удалось подключить Firebase. Проверьте firebase-config.json.');
    }
  }

  async save(id: string, fields: Omit<Menu, 'id'>) {
    const { dishes } = await this.connection;
    if (id) {
      await updateDoc(doc(dishes, id), fields);
    } else {
      await addDoc(dishes, fields);
    }
  }

  async remove(id: string) {
    const { dishes } = await this.connection;
    await deleteDoc(doc(dishes, id));
  }

  async signIn(email: string, password: string) {
    const { auth } = await this.connection;
    await signInWithEmailAndPassword(auth, email, password);
  }

  async signOut() {
    const { auth } = await this.connection;
    await signOut(auth);
  }
}
