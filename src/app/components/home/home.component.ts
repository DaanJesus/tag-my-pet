import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, HostListener, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
import { TimelineMax, Power2, Power4 } from 'gsap';
import * as $ from 'jquery';
import { timestamp } from 'rxjs';
import { AuthService } from 'src/app/services/auth.service';
import { LoadingService } from 'src/app/services/loading.service';
import { PostService } from 'src/app/services/post.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, AfterViewInit {

  @ViewChild('feedContent', { static: false }) feedContent!: ElementRef;
  @ViewChild('fileInput', { static: false }) fileInput!: ElementRef;

  @Input() post: any;
  isLiked: boolean = false;

  posts: any = [];
  page = 1;
  hasMorePosts = true;
  isLoading: boolean = false;

  user: any;

  isMouseOver: boolean = false
  textPost: string = '';

  isExpanded: boolean = false;

  selectedImage: string | ArrayBuffer | null = null;

  constructor(
    public loadingService: LoadingService,
    private postService: PostService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }
  ngAfterViewInit(): void {
    this.feedContent.nativeElement.addEventListener('scroll', this.onScroll.bind(this));
  }

  toggleExpand() {
    this.isExpanded = !this.isExpanded;
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const reader = new FileReader();

      reader.onload = () => {
        const img = new Image();
        img.src = reader.result as string;

        img.onload = () => {
          const MAX_WIDTH = 300;
          const MAX_HEIGHT = 300;
          let width = img.width;
          let height = img.height;

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

          const canvas = document.createElement('canvas');
          canvas.width = width;
          canvas.height = height;

          const ctx = canvas.getContext('2d');
          if (ctx) {
            ctx.drawImage(img, 0, 0, width, height);
            this.selectedImage = canvas.toDataURL('image/jpeg');
            this.cdr.detectChanges();
          }
        };
      };

      reader.readAsDataURL(file);
    }
  }

  // Reseta o campo de input utilizando ViewChild
  resetFileInput(): void {
    if (this.fileInput) {
      this.fileInput.nativeElement.value = ''; // Reseta o campo de input
    }
  }

  ngOnInit(): void {

    this.loadPosts();

    this.authService.user$.subscribe(user => {
      this.user = user
    })

  }

  loadPosts(): void {
    if (!this.hasMorePosts || this.isLoading) {
      return;
    }

    this.isLoading = true;

    this.postService.getPosts(this.page).subscribe(
      (data: any) => {
        if (data.posts.length > 0) {
          this.posts.push(...data.posts);
          this.page++;
        } else {
          this.hasMorePosts = false;
        }

        this.isLoading = false;
      }, (error) => {
        this.isLoading = false;
        console.error(error);
      });
  }

  onScroll(): void {
    const element = this.feedContent.nativeElement;
    const scrollHeight = element.scrollHeight;
    const scrollTop = element.scrollTop;
    const clientHeight = element.clientHeight;

    if (scrollTop + clientHeight >= scrollHeight * 0.8 && !this.isLoading) {
      this.loadPosts();
    }
  }

  toggleLike(postId: string, index: number) {
    this.postService.toggleLike(postId, this.user._id).subscribe((updatedPost) => {
      this.posts[index] = updatedPost;
    });
  }

  registerPost() {

    if (this.textPost.trim() === '') {
      return;
    }

    const post: any = {
      content: this.textPost,
      author: this.user,
    };

    if (this.selectedImage) {
      post.image = this.selectedImage;
    }

    this.postService.registerPost(post).subscribe(res => {
      // Em vez de usar push, utilize unshift para adicionar o post no início
      this.posts = [res, ...this.posts];
      this.textPost = '';
      this.selectedImage = null;
      this.resetFileInput();
    });
  }
}