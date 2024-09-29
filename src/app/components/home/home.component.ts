import { AfterViewInit, Component, ElementRef, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
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

  constructor(
    private postService: PostService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {

    this.postService.getPosts().subscribe(posts => {
      this.posts = posts
      console.log(posts);

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

  registerPost(textPost: any) {
    const post = {
      content: textPost,
      author: this.user,
    }

    this.postService.registerPost(post).subscribe(res => {
      console.log(res);

      this.posts.push(res)
    })
  }
}