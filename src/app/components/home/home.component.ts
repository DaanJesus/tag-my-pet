import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
import { TimelineMax, Power2, Power4 } from 'gsap';
import * as $ from 'jquery';
import { timestamp } from 'rxjs';
import { AuthService } from 'src/app/services/auth.service';
import { PostService } from 'src/app/services/post.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  @Input() post: any; // Receber o post como input
  isLiked: boolean = false;

  posts: any = [];
  user: any;

  isMouseOver: boolean = false
  textPost: string = '';

  isExpanded: boolean = false;

  selectedImage: string | ArrayBuffer | null = null;

  constructor(
    private postService: PostService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  toggleExpand() {
    this.isExpanded = !this.isExpanded;
  }

  onImageSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const reader = new FileReader();

      reader.onload = () => {
        const img = new Image();
        img.src = reader.result as string;

        img.onload = () => {
          // Configurações do canvas
          const MAX_WIDTH = 300; // Defina a largura máxima desejada
          const MAX_HEIGHT = 300; // Defina a altura máxima desejada
          let width = img.width;
          let height = img.height;

          // Calcula a nova largura e altura
          if (width > height) {
            if (width > MAX_WIDTH) {
              height *= MAX_WIDTH / width;
              width = MAX_WIDTH;
            }
          } else {
            if (height > MAX_HEIGHT) {
              width *= MAX_HEIGHT / height;
              height = MAX_HEIGHT;
            }
          }

          // Cria um canvas para redimensionar a imagem
          const canvas = document.createElement('canvas');
          canvas.width = width;
          canvas.height = height;

          const ctx = canvas.getContext('2d');
          if (ctx) {
            ctx.drawImage(img, 0, 0, width, height);
            this.selectedImage = canvas.toDataURL('image/jpeg'); // Converte a imagem redimensionada para Data URL
            this.cdr.detectChanges(); // Força a detecção de mudanças
          }
        };
      };

      reader.readAsDataURL(file); // Lê a imagem como Data URL
    }
  }


  ngOnInit(): void {

    this.postService.getPosts().subscribe(posts => {
      this.posts = posts
    })

    this.authService.user$.subscribe(user => {
      this.user = user
    })
  }

  toggleLike(postId: string, index: number) {
    this.postService.toggleLike(postId, this.user._id).subscribe((updatedPost) => {
      this.posts[index] = updatedPost;
    });
  }

  onMouseEnter() {
    this.isMouseOver = true;
  }

  onMouseLeave() {
    this.isMouseOver = false;
  }

  registerPost() {
    const post: any = {
      content: this.textPost,
      author: this.user,
    };

    if (this.selectedImage) {
      post.image = this.selectedImage
    }

    this.postService.registerPost(post).subscribe(res => {
      this.posts.push(res);
      this.textPost = '';
    });
  }
}